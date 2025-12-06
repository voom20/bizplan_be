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
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        log.info("사업계획서 전체 생성 시작: projectId={}", projectId);
        long startTime = System.currentTimeMillis();

        // 1. 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

        // 2. Wizard 답변 파싱
        Map<String, Object> context = parseWizardAnswers(project.getWizardAnswers());
        if (context.isEmpty()) {
            throw new IllegalStateException("Wizard 답변이 없습니다. 먼저 Wizard를 완료해주세요.");
        }

        // 3. 새 문서 생성
        int nextVersion = documentRepository.findMaxVersionByProjectId(projectId) + 1;
        BusinessPlanDocument newDocument = BusinessPlanDocument.createNew(projectId, nextVersion);
        BusinessPlanDocument document = Objects.requireNonNull(
                documentRepository.save(newDocument), "문서 저장 실패");

        // 4. 섹션별 생성 (순차적)
        String templateCode = project.getTemplateCode().name();
        Map<String, String> generatedSections = new LinkedHashMap<>();

        try {
            for (String sectionType : SECTION_ORDER) {
                log.debug("섹션 생성 중: projectId={}, section={}", projectId, sectionType);
                
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
                    generatedSections.put(sectionType, content);
                    updateDocumentSection(document, sectionType, content);
                }
            }

            // 5. 완료 처리
            int totalTimeMs = (int) (System.currentTimeMillis() - startTime);
            document.markAsGenerated(totalTimeMs);
            document = documentRepository.save(document);

            log.info("사업계획서 전체 생성 완료: projectId={}, documentId={}, version={}, timeMs={}",
                    projectId, document.getId(), document.getVersion(), totalTimeMs);

            return document;

        } catch (Exception e) {
            log.error("사업계획서 생성 실패: projectId={}", projectId, e);
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
        Objects.requireNonNull(documentId, "문서 ID는 필수입니다");
        Objects.requireNonNull(sectionType, "섹션 타입은 필수입니다");
        log.info("섹션 재생성: documentId={}, section={}", documentId, sectionType);

        // 1. 문서 조회
        BusinessPlanDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));

        // 2. 프로젝트 조회
        String projectId = Objects.requireNonNull(document.getProjectId(), "문서에 프로젝트 ID가 없습니다");
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다"));

        // 3. 컨텍스트 및 이전 섹션 준비
        Map<String, Object> context = parseWizardAnswers(project.getWizardAnswers());
        Map<String, String> previousSections = extractPreviousSections(document, sectionType);

        // 4. AI 엔진 호출
        BizPlanGenerateRequest request = BizPlanGenerateRequest.withPreviousSections(
                document.getProjectId(),
                project.getTemplateCode().name(),
                sectionType,
                context,
                previousSections
        );

        BizPlanGenerateResponse response = aiEngineClient.generateSection(request);

        // 5. 문서 업데이트
        if (response != null && response.section() != null) {
            updateDocumentSection(document, sectionType, response.section().content());
            document = documentRepository.save(document);
        }

        return document;
    }

    /**
     * 프로젝트의 최신 문서 조회.
     */
    public Optional<BusinessPlanDocument> getLatestDocument(String projectId) {
        return documentRepository.findFirstByProjectIdOrderByVersionDesc(projectId);
    }

    /**
     * 문서 ID로 조회.
     *
     * @param documentId 문서 ID (null 불가)
     * @return 문서 (Optional)
     */
    public Optional<BusinessPlanDocument> getDocument(String documentId) {
        Objects.requireNonNull(documentId, "문서 ID는 필수입니다");
        return documentRepository.findById(documentId);
    }

    /**
     * 프로젝트의 모든 문서 버전 조회.
     */
    public List<BusinessPlanDocument> getAllVersions(String projectId) {
        return documentRepository.findByProjectIdOrderByVersionDesc(projectId);
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

