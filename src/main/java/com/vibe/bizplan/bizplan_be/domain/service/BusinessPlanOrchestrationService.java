package com.vibe.bizplan.bizplan_be.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.infrastructure.client.AiEngineClient;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.BizPlanGenerateRequest;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.BizPlanGenerateResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.BusinessPlanDocumentRepository;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 사업계획서 생성 오케스트레이션 서비스.
 * Wizard 답변을 조합하여 AI 엔진에 생성을 요청하고, 결과를 DB에 저장한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessPlanOrchestrationService {

    private final ProjectRepository projectRepository;
    private final BusinessPlanDocumentRepository documentRepository;
    private final AiEngineClient aiEngineClient;
    private final ObjectMapper objectMapper;

    /** 생성할 섹션 순서 */
    private static final List<String> SECTION_ORDER = List.of(
            "executive_summary",
            "problem_definition",
            "solution",
            "market_analysis",
            "business_model",
            "competitive_analysis",
            "marketing_strategy",
            "team",
            "financial_plan",
            "milestones"
    );

    /**
     * 사업계획서 전체 생성.
     * 모든 섹션을 순차적으로 생성하여 하나의 문서로 저장한다.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @return 생성된 문서
     */
    @SuppressWarnings("null") // Spring Data save() 메서드의 null-safety 분석 오탐 억제
    @Transactional
    public BusinessPlanDocument generateFullDocument(String projectId) {
        // [Stage 1] 요청 검증 및 시작
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        long startTime = System.currentTimeMillis();
        log.info("[BizPlan] 사업계획서 전체 생성 시작 - projectId={}, totalSections={}", 
                projectId, SECTION_ORDER.size());

        // [Stage 2] 프로젝트 조회
        log.debug("[BizPlan] 프로젝트 조회 중 - projectId={}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.error("[BizPlan] 프로젝트 조회 실패 - projectId={} (존재하지 않음)", projectId);
                    return new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId);
                });
        
        String templateCode = project.getTemplateCode().name();
        log.debug("[BizPlan] 프로젝트 조회 완료 - projectId={}, templateCode={}, status={}", 
                projectId, templateCode, project.getStatus());

        // [Stage 3] Wizard 답변 파싱 및 검증
        log.debug("[BizPlan] Wizard 답변 파싱 중 - projectId={}", projectId);
        Map<String, Object> context = parseWizardAnswers(project.getWizardAnswers());
        if (context.isEmpty()) {
            log.error("[BizPlan] Wizard 답변 없음 - projectId={}", projectId);
            throw new IllegalStateException("Wizard 답변이 없습니다. 먼저 Wizard를 완료해주세요.");
        }
        log.debug("[BizPlan] Wizard 답변 파싱 완료 - projectId={}, contextKeys={}", projectId, context.keySet());

        // [Stage 4] 새 문서 생성
        int nextVersion = documentRepository.findMaxVersionByProjectId(projectId) + 1;
        log.debug("[BizPlan] 새 문서 생성 중 - projectId={}, version={}", projectId, nextVersion);
        BusinessPlanDocument newDocument = BusinessPlanDocument.createNew(projectId, nextVersion);
        BusinessPlanDocument document = Objects.requireNonNull(
                documentRepository.save(newDocument), "문서 저장 실패");
        String documentId = document.getId();
        log.info("[BizPlan] 문서 생성 완료 - projectId={}, documentId={}, version={}", 
                projectId, documentId, nextVersion);

        // [Stage 5] 섹션별 생성 (순차적)
        Map<String, String> generatedSections = new LinkedHashMap<>();
        int sectionIndex = 0;

        try {
            for (String sectionType : SECTION_ORDER) {
                sectionIndex++;
                long sectionStartTime = System.currentTimeMillis();
                log.info("[BizPlan] 섹션 생성 시작 - projectId={}, documentId={}, section={} ({}/{})", 
                        projectId, documentId, sectionType, sectionIndex, SECTION_ORDER.size());
                
                BizPlanGenerateRequest request = BizPlanGenerateRequest.withPreviousSections(
                        projectId,
                        templateCode,
                        sectionType,
                        context,
                        generatedSections
                );

                BizPlanGenerateResponse response = aiEngineClient.generateSection(request);
                
                if (response != null && response.section() != null) {
                    String content = response.section().content();
                    int contentLength = content != null ? content.length() : 0;
                    generatedSections.put(sectionType, content);
                    updateDocumentSection(document, sectionType, content);
                    
                    long sectionDuration = System.currentTimeMillis() - sectionStartTime;
                    log.info("[BizPlan] 섹션 생성 완료 - projectId={}, section={}, contentLength={}, duration={}ms", 
                            projectId, sectionType, contentLength, sectionDuration);
                } else {
                    log.warn("[BizPlan] 섹션 생성 응답 없음 - projectId={}, section={}", projectId, sectionType);
                }
            }

            // [Stage 6] 완료 처리
            int totalTimeMs = (int) (System.currentTimeMillis() - startTime);
            document.markAsGenerated(totalTimeMs);
            document = documentRepository.save(document);

            log.info("[BizPlan] 사업계획서 전체 생성 완료 - projectId={}, documentId={}, version={}, " +
                    "sectionsGenerated={}/{}, totalWordCount={}, duration={}ms",
                    projectId, documentId, document.getVersion(), 
                    generatedSections.size(), SECTION_ORDER.size(), 
                    document.getTotalWordCount(), totalTimeMs);

            return document;

        } catch (Exception e) {
            long totalTimeMs = System.currentTimeMillis() - startTime;
            log.error("[BizPlan] 사업계획서 생성 실패 - projectId={}, documentId={}, " +
                    "completedSections={}/{}, duration={}ms", 
                    projectId, documentId, sectionIndex - 1, SECTION_ORDER.size(), totalTimeMs, e);
            document.markAsFailed();
            documentRepository.save(document);
            throw new RuntimeException("사업계획서 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 단일 섹션 생성 (재생성용).
     *
     * @param documentId 문서 ID (null 불가)
     * @param sectionType 섹션 타입 (null 불가)
     * @return 업데이트된 문서
     */
    @Transactional
    public BusinessPlanDocument regenerateSection(String documentId, String sectionType) {
        // [Stage 1] 요청 검증
        Objects.requireNonNull(documentId, "문서 ID는 필수입니다");
        Objects.requireNonNull(sectionType, "섹션 타입은 필수입니다");
        long startTime = System.currentTimeMillis();
        log.info("[BizPlan] 섹션 재생성 시작 - documentId={}, sectionType={}", documentId, sectionType);

        try {
            // [Stage 2] 문서 조회
            log.debug("[BizPlan] 문서 조회 중 - documentId={}", documentId);
            BusinessPlanDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> {
                        log.error("[BizPlan] 문서 조회 실패 - documentId={} (존재하지 않음)", documentId);
                        return new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId);
                    });

            // [Stage 3] 프로젝트 조회
            String projectId = Objects.requireNonNull(document.getProjectId(), "문서에 프로젝트 ID가 없습니다");
            log.debug("[BizPlan] 프로젝트 조회 중 - projectId={}", projectId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> {
                        log.error("[BizPlan] 프로젝트 조회 실패 - projectId={}", projectId);
                        return new IllegalArgumentException("프로젝트를 찾을 수 없습니다");
                    });
            
            String templateCode = project.getTemplateCode().name();
            log.debug("[BizPlan] 프로젝트 조회 완료 - projectId={}, templateCode={}", projectId, templateCode);

            // [Stage 4] 컨텍스트 및 이전 섹션 준비
            Map<String, Object> context = parseWizardAnswers(project.getWizardAnswers());
            Map<String, String> previousSections = extractPreviousSections(document, sectionType);
            log.debug("[BizPlan] 컨텍스트 준비 완료 - previousSectionsCount={}", previousSections.size());

            // [Stage 5] AI 엔진 호출
            log.debug("[BizPlan] AI 엔진 호출 중 - documentId={}, sectionType={}", documentId, sectionType);
            BizPlanGenerateRequest request = BizPlanGenerateRequest.withPreviousSections(
                    document.getProjectId(),
                    templateCode,
                    sectionType,
                    context,
                    previousSections
            );

            BizPlanGenerateResponse response = aiEngineClient.generateSection(request);

            // [Stage 6] 문서 업데이트 및 완료
            if (response != null && response.section() != null) {
                String content = response.section().content();
                int contentLength = content != null ? content.length() : 0;
                updateDocumentSection(document, sectionType, content);
                document = documentRepository.save(document);
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("[BizPlan] 섹션 재생성 완료 - documentId={}, sectionType={}, contentLength={}, duration={}ms", 
                        documentId, sectionType, contentLength, duration);
            } else {
                log.warn("[BizPlan] 섹션 재생성 응답 없음 - documentId={}, sectionType={}", documentId, sectionType);
            }

            return document;
            
        } catch (IllegalArgumentException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[BizPlan] 섹션 재생성 실패 (비즈니스 오류) - documentId={}, sectionType={}, error={}, duration={}ms", 
                    documentId, sectionType, e.getMessage(), duration);
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[BizPlan] 섹션 재생성 실패 (시스템 오류) - documentId={}, sectionType={}, duration={}ms", 
                    documentId, sectionType, duration, e);
            throw e;
        }
    }

    /**
     * 프로젝트의 최신 문서 조회.
     */
    public Optional<BusinessPlanDocument> getLatestDocument(String projectId) {
        log.debug("[BizPlan] 최신 문서 조회 - projectId={}", projectId);
        Optional<BusinessPlanDocument> document = documentRepository.findFirstByProjectIdOrderByVersionDesc(projectId);
        document.ifPresentOrElse(
                doc -> log.debug("[BizPlan] 최신 문서 조회 완료 - projectId={}, documentId={}, version={}", 
                        projectId, doc.getId(), doc.getVersion()),
                () -> log.debug("[BizPlan] 최신 문서 없음 - projectId={}", projectId)
        );
        return document;
    }

    /**
     * 문서 ID로 조회.
     *
     * @param documentId 문서 ID (null 불가)
     * @return 문서 (Optional)
     */
    public Optional<BusinessPlanDocument> getDocument(String documentId) {
        Objects.requireNonNull(documentId, "문서 ID는 필수입니다");
        log.debug("[BizPlan] 문서 조회 - documentId={}", documentId);
        Optional<BusinessPlanDocument> document = documentRepository.findById(documentId);
        document.ifPresentOrElse(
                doc -> log.debug("[BizPlan] 문서 조회 완료 - documentId={}, projectId={}, version={}", 
                        documentId, doc.getProjectId(), doc.getVersion()),
                () -> log.debug("[BizPlan] 문서 없음 - documentId={}", documentId)
        );
        return document;
    }

    /**
     * 프로젝트의 모든 문서 버전 조회.
     */
    public List<BusinessPlanDocument> getAllVersions(String projectId) {
        log.debug("[BizPlan] 전체 버전 조회 - projectId={}", projectId);
        List<BusinessPlanDocument> documents = documentRepository.findByProjectIdOrderByVersionDesc(projectId);
        log.debug("[BizPlan] 전체 버전 조회 완료 - projectId={}, count={}", projectId, documents.size());
        return documents;
    }

    // ==========================================
    // Private Helper Methods
    // ==========================================

    /**
     * Wizard 답변 JSON 파싱.
     */
    private Map<String, Object> parseWizardAnswers(String wizardAnswersJson) {
        if (wizardAnswersJson == null || wizardAnswersJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(wizardAnswersJson, new TypeReference<Map<String, Object>>() {});
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("Wizard 답변 파싱 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 섹션 타입에 따라 문서 업데이트.
     */
    private void updateDocumentSection(BusinessPlanDocument document, String sectionType, String content) {
        switch (sectionType) {
            case "executive_summary" -> document.updateExecutiveSummary(content);
            case "problem_definition" -> document.updateProblemDefinition(content);
            case "solution" -> document.updateSolution(content);
            case "market_analysis" -> document.updateMarketAnalysis(content);
            case "business_model" -> document.updateBusinessModel(content);
            case "competitive_analysis" -> document.updateCompetitiveAnalysis(content);
            case "marketing_strategy" -> document.updateMarketingStrategy(content);
            case "team" -> document.updateTeam(content);
            case "financial_plan" -> document.updateFinancialPlan(content);
            case "milestones" -> document.updateMilestones(content);
            default -> log.warn("알 수 없는 섹션 타입: {}", sectionType);
        }
    }

    /**
     * 현재 섹션 이전에 생성된 섹션들 추출.
     */
    private Map<String, String> extractPreviousSections(BusinessPlanDocument doc, String currentSection) {
        Map<String, String> previous = new LinkedHashMap<>();
        
        for (String section : SECTION_ORDER) {
            if (section.equals(currentSection)) break;
            
            String content = getSectionContent(doc, section);
            if (content != null && !content.isBlank()) {
                previous.put(section, content);
            }
        }
        
        return previous;
    }

    /**
     * 문서에서 섹션 내용 추출.
     */
    private String getSectionContent(BusinessPlanDocument doc, String sectionType) {
        return switch (sectionType) {
            case "executive_summary" -> doc.getExecutiveSummary();
            case "problem_definition" -> doc.getProblemDefinition();
            case "solution" -> doc.getSolution();
            case "market_analysis" -> doc.getMarketAnalysis();
            case "business_model" -> doc.getBusinessModel();
            case "competitive_analysis" -> doc.getCompetitiveAnalysis();
            case "marketing_strategy" -> doc.getMarketingStrategy();
            case "team" -> doc.getTeam();
            case "financial_plan" -> doc.getFinancialPlan();
            case "milestones" -> doc.getMilestones();
            default -> null;
        };
    }
}

