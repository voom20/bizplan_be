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
     * @param projectId 프로젝트 ID
     * @param format 출력 포맷
     * @return 파일 바이트 배열
     */
    public byte[] exportDocument(String projectId, ExportFormat format) {
        log.info("문서 내보내기 시작: projectId={}, format={}", projectId, format);
        
        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        // 최신 문서 조회
        BusinessPlanDocument document = documentRepository.findFirstByProjectIdOrderByVersionDesc(projectId)
                .orElseThrow(() -> new IllegalStateException("생성된 사업계획서가 없습니다. 먼저 문서를 생성해주세요."));
        
        // HTML 렌더링
        String html = renderHtml(project, document);
        
        return switch (format) {
            case PDF -> convertToPdf(html);
            case HTML -> html.getBytes(StandardCharsets.UTF_8);
        };
    }

    /**
     * 특정 버전의 문서 내보내기.
     */
    public byte[] exportDocumentVersion(String projectId, int version, ExportFormat format) {
        log.info("문서 내보내기: projectId={}, version={}, format={}", projectId, version, format);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다"));
        
        BusinessPlanDocument document = documentRepository.findByProjectIdAndVersion(projectId, version)
                .orElseThrow(() -> new IllegalArgumentException("해당 버전의 문서를 찾을 수 없습니다: v" + version));
        
        String html = renderHtml(project, document);
        
        return switch (format) {
            case PDF -> convertToPdf(html);
            case HTML -> html.getBytes(StandardCharsets.UTF_8);
        };
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
     */
    private byte[] convertToPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            log.info("PDF 생성 완료: size={}KB", outputStream.size() / 1024);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("PDF 변환 실패", e);
            throw new RuntimeException("PDF 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일명 생성.
     */
    public String generateFileName(String projectId, ExportFormat format) {
        Project project = projectRepository.findById(projectId).orElse(null);
        String title = project != null && project.getTitle() != null 
                ? project.getTitle().replaceAll("[^a-zA-Z0-9가-힣]", "_")
                : "bizplan";
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s%s", title, timestamp, format.getExtension());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
    }
}

