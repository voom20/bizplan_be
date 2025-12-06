package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ExportFormat;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.BusinessPlanDocumentRepository;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 문서 내보내기 서비스.
 * 사업계획서를 PDF/HTML 형식으로 변환하여 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExportService {

    private final ProjectRepository projectRepository;
    private final BusinessPlanDocumentRepository documentRepository;
    private final TemplateEngine templateEngine;

    /**
     * 사업계획서를 지정된 포맷으로 내보내기.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @param format 출력 포맷 (null 불가)
     * @return 파일 바이트 배열
     */
    public byte[] exportDocument(String projectId, ExportFormat format) {
        // [Stage 1] 요청 검증
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(format, "출력 포맷은 필수입니다");
        long startTime = System.currentTimeMillis();
        log.info("[Export] 문서 내보내기 시작 - projectId={}, format={}", projectId, format);
        
        try {
            // [Stage 2] 프로젝트 조회
            log.debug("[Export] 프로젝트 조회 중 - projectId={}", projectId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> {
                        log.error("[Export] 프로젝트 조회 실패 - projectId={} (존재하지 않음)", projectId);
                        return new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId);
                    });
            
            String templateCode = project.getTemplateCode().name();
            log.debug("[Export] 프로젝트 조회 완료 - projectId={}, templateCode={}", projectId, templateCode);
            
            // [Stage 3] 최신 문서 조회
            log.debug("[Export] 최신 문서 조회 중 - projectId={}", projectId);
            BusinessPlanDocument document = documentRepository.findFirstByProjectIdOrderByVersionDesc(projectId)
                    .orElseThrow(() -> {
                        log.error("[Export] 문서 조회 실패 - projectId={} (문서 없음)", projectId);
                        return new IllegalStateException("생성된 사업계획서가 없습니다. 먼저 문서를 생성해주세요.");
                    });
            
            log.debug("[Export] 문서 조회 완료 - projectId={}, documentId={}, version={}", 
                    projectId, document.getId(), document.getVersion());
            
            // [Stage 4] HTML 렌더링
            log.debug("[Export] HTML 렌더링 시작 - projectId={}", projectId);
            String html = renderHtml(project, document);
            log.debug("[Export] HTML 렌더링 완료 - projectId={}, htmlLength={}", projectId, html.length());
            
            // [Stage 5] 포맷 변환 및 완료
            byte[] result = switch (format) {
                case PDF -> convertToPdf(html, projectId);
                case HTML -> html.getBytes(StandardCharsets.UTF_8);
            };
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Export] 문서 내보내기 완료 - projectId={}, format={}, version={}, sizeKB={}, duration={}ms", 
                    projectId, format, document.getVersion(), result.length / 1024, duration);
            
            return result;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Export] 문서 내보내기 실패 (비즈니스 오류) - projectId={}, format={}, error={}, duration={}ms", 
                    projectId, format, e.getMessage(), duration);
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Export] 문서 내보내기 실패 (시스템 오류) - projectId={}, format={}, duration={}ms", 
                    projectId, format, duration, e);
            throw e;
        }
    }

    /**
     * 특정 버전의 문서 내보내기.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @param version 문서 버전
     * @param format 출력 포맷 (null 불가)
     * @return 파일 바이트 배열
     */
    public byte[] exportDocumentVersion(String projectId, int version, ExportFormat format) {
        // [Stage 1] 요청 검증
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(format, "출력 포맷은 필수입니다");
        long startTime = System.currentTimeMillis();
        log.info("[Export] 버전별 문서 내보내기 시작 - projectId={}, version={}, format={}", projectId, version, format);
        
        try {
            // [Stage 2] 프로젝트 조회
            log.debug("[Export] 프로젝트 조회 중 - projectId={}", projectId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> {
                        log.error("[Export] 프로젝트 조회 실패 - projectId={}", projectId);
                        return new IllegalArgumentException("프로젝트를 찾을 수 없습니다");
                    });
            
            // [Stage 3] 특정 버전 문서 조회
            log.debug("[Export] 문서 조회 중 - projectId={}, version={}", projectId, version);
            BusinessPlanDocument document = documentRepository.findByProjectIdAndVersion(projectId, version)
                    .orElseThrow(() -> {
                        log.error("[Export] 문서 조회 실패 - projectId={}, version={} (해당 버전 없음)", projectId, version);
                        return new IllegalArgumentException("해당 버전의 문서를 찾을 수 없습니다: v" + version);
                    });
            
            log.debug("[Export] 문서 조회 완료 - projectId={}, documentId={}, version={}", 
                    projectId, document.getId(), version);
            
            // [Stage 4] HTML 렌더링 및 변환
            String html = renderHtml(project, document);
            
            byte[] result = switch (format) {
                case PDF -> convertToPdf(html, projectId);
                case HTML -> html.getBytes(StandardCharsets.UTF_8);
            };
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Export] 버전별 문서 내보내기 완료 - projectId={}, version={}, format={}, sizeKB={}, duration={}ms", 
                    projectId, version, format, result.length / 1024, duration);
            
            return result;
            
        } catch (IllegalArgumentException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Export] 버전별 문서 내보내기 실패 - projectId={}, version={}, error={}, duration={}ms", 
                    projectId, version, e.getMessage(), duration);
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Export] 버전별 문서 내보내기 실패 (시스템 오류) - projectId={}, version={}, duration={}ms", 
                    projectId, version, duration, e);
            throw e;
        }
    }

    /**
     * HTML 템플릿 렌더링.
     */
    private String renderHtml(Project project, BusinessPlanDocument document) {
        Context context = new Context();
        
        // 프로젝트 정보
        context.setVariable("projectTitle", project.getTitle() != null ? project.getTitle() : "사업계획서");
        context.setVariable("templateName", project.getTemplateCode().getDisplayName());
        context.setVariable("version", document.getVersion());
        context.setVariable("createdAt", formatDateTime(document.getCreatedAt()));
        
        // 섹션별 내용
        Map<String, String> sections = new HashMap<>();
        sections.put("executiveSummary", document.getExecutiveSummary());
        sections.put("problemDefinition", document.getProblemDefinition());
        sections.put("solution", document.getSolution());
        sections.put("marketAnalysis", document.getMarketAnalysis());
        sections.put("businessModel", document.getBusinessModel());
        sections.put("competitiveAnalysis", document.getCompetitiveAnalysis());
        sections.put("marketingStrategy", document.getMarketingStrategy());
        sections.put("team", document.getTeam());
        sections.put("financialPlan", document.getFinancialPlan());
        sections.put("milestones", document.getMilestones());
        context.setVariable("sections", sections);
        
        // 메타데이터
        context.setVariable("totalWordCount", document.getTotalWordCount());
        
        return templateEngine.process("business-plan-template", context);
    }

    /**
     * HTML을 PDF로 변환.
     * 
     * @param html HTML 문자열
     * @param projectId 프로젝트 ID (로깅용)
     * @return PDF 바이트 배열
     */
    private byte[] convertToPdf(String html, String projectId) {
        long startTime = System.currentTimeMillis();
        log.debug("[Export] PDF 변환 시작 - projectId={}, htmlLength={}", projectId, html.length());
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Export] PDF 변환 완료 - projectId={}, sizeKB={}, duration={}ms", 
                    projectId, outputStream.size() / 1024, duration);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Export] PDF 변환 실패 - projectId={}, htmlLength={}, duration={}ms", 
                    projectId, html.length(), duration, e);
            throw new RuntimeException("PDF 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일명 생성.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @param format 출력 포맷 (null 불가)
     * @return 생성된 파일명
     */
    public String generateFileName(String projectId, ExportFormat format) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(format, "출력 포맷은 필수입니다");
        
        log.debug("[Export] 파일명 생성 - projectId={}, format={}", projectId, format);
        
        Project project = projectRepository.findById(projectId).orElse(null);
        String title = project != null && project.getTitle() != null 
                ? project.getTitle().replaceAll("[^a-zA-Z0-9가-힣]", "_")
                : "bizplan";
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%s%s", title, timestamp, format.getExtension());
        
        log.debug("[Export] 파일명 생성 완료 - projectId={}, fileName={}", projectId, fileName);
        return fileName;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
    }
}

