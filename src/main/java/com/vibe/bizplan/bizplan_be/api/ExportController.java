package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.model.ExportFormat;
import com.vibe.bizplan.bizplan_be.domain.service.DocumentExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 문서 내보내기 REST API 컨트롤러.
 * 사업계획서를 PDF/HTML 형식으로 다운로드하는 API를 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
@Tag(name = "Document Export", description = "사업계획서 내보내기 API")
public class ExportController {

    private final DocumentExportService exportService;

    /**
     * 사업계획서 내보내기.
     * 최신 버전의 문서를 지정된 형식으로 다운로드한다.
     *
     * @param projectId 프로젝트 ID
     * @param format 출력 형식 (pdf, html)
     * @return 파일 바이트 스트림
     */
    @GetMapping("/export")
    @Operation(
            summary = "사업계획서 내보내기",
            description = "생성된 사업계획서를 PDF 또는 HTML 형식으로 다운로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내보내기 성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 형식"),
            @ApiResponse(responseCode = "404", description = "문서 없음")
    })
    public ResponseEntity<byte[]> exportDocument(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "출력 형식 (pdf, html)") @RequestParam(defaultValue = "pdf") String format
    ) {
        log.info("GET /projects/{}/export?format={}", projectId, format);
        
        ExportFormat exportFormat = parseFormat(format);
        byte[] fileContent = exportService.exportDocument(projectId, exportFormat);
        String fileName = exportService.generateFileName(projectId, exportFormat);
        
        return buildFileResponse(fileContent, fileName, exportFormat);
    }

    /**
     * 특정 버전의 문서 내보내기.
     */
    @GetMapping("/export/versions/{version}")
    @Operation(
            summary = "특정 버전 내보내기",
            description = "지정된 버전의 사업계획서를 다운로드합니다."
    )
    public ResponseEntity<byte[]> exportDocumentVersion(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "문서 버전") @PathVariable int version,
            @Parameter(description = "출력 형식 (pdf, html)") @RequestParam(defaultValue = "pdf") String format
    ) {
        log.info("GET /projects/{}/export/versions/{}?format={}", projectId, version, format);
        
        ExportFormat exportFormat = parseFormat(format);
        byte[] fileContent = exportService.exportDocumentVersion(projectId, version, exportFormat);
        String fileName = exportService.generateFileName(projectId, exportFormat);
        
        return buildFileResponse(fileContent, fileName, exportFormat);
    }

    /**
     * 지원하는 내보내기 형식 목록.
     */
    @GetMapping("/export/formats")
    @Operation(
            summary = "지원 형식 목록",
            description = "사용 가능한 내보내기 형식을 반환합니다."
    )
    public ResponseEntity<?> getSupportedFormats() {
        return ResponseEntity.ok(new Object() {
            public final String[] formats = {"pdf", "html"};
            public final String defaultFormat = "pdf";
            public final String note = "HWP 형식은 향후 지원 예정입니다.";
        });
    }

    /**
     * 형식 문자열 파싱.
     */
    private ExportFormat parseFormat(String format) {
        try {
            return ExportFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 형식: {}", format);
            throw new IllegalArgumentException("지원하지 않는 형식입니다: " + format + ". 사용 가능: pdf, html");
        }
    }

    /**
     * 파일 다운로드 응답 생성.
     */
    private ResponseEntity<byte[]> buildFileResponse(byte[] content, String fileName, ExportFormat format) {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(format.getContentType()));
        headers.setContentLength(content.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        
        log.info("파일 다운로드 응답: fileName={}, size={}KB", fileName, content.length / 1024);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}

