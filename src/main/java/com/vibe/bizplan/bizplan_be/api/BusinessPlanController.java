package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import com.vibe.bizplan.bizplan_be.domain.service.BusinessPlanOrchestrationService;
import com.vibe.bizplan.bizplan_be.dto.response.BusinessPlanDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사업계획서 생성 REST API 컨트롤러.
 * AI 엔진과 연동하여 사업계획서를 생성하는 오케스트레이션 API.
 */
@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/documents")
@RequiredArgsConstructor
@Tag(name = "Business Plan Documents", description = "사업계획서 생성 및 관리 API")
public class BusinessPlanController {

    private final BusinessPlanOrchestrationService orchestrationService;

    /**
     * 사업계획서 전체 생성.
     * Wizard 답변을 기반으로 모든 섹션을 AI로 생성한다.
     *
     * @param projectId 프로젝트 ID
     * @return 생성된 문서
     */
    @PostMapping("/business-plan/generate")
    @Operation(
            summary = "사업계획서 전체 생성",
            description = "프로젝트의 Wizard 답변을 기반으로 AI가 사업계획서 전체를 생성합니다. " +
                    "새 버전의 문서가 생성됩니다. (동기 호출, 최대 60초 소요)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = BusinessPlanDocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (Wizard 미완료 등)"),
            @ApiResponse(responseCode = "500", description = "AI 엔진 오류")
    })
    public ResponseEntity<BusinessPlanDocumentResponse> generateBusinessPlan(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId
    ) {
        log.info("POST /projects/{}/documents/business-plan/generate", projectId);
        
        BusinessPlanDocument document = orchestrationService.generateFullDocument(projectId);
        
        log.info("사업계획서 생성 완료: projectId={}, documentId={}, version={}",
                projectId, document.getId(), document.getVersion());
        
        return ResponseEntity.ok(BusinessPlanDocumentResponse.from(document));
    }

    /**
     * 특정 섹션 재생성.
     *
     * @param projectId 프로젝트 ID
     * @param documentId 문서 ID
     * @param sectionType 재생성할 섹션 타입
     * @return 업데이트된 문서
     */
    @PostMapping("/{documentId}/sections/{sectionType}/regenerate")
    @Operation(
            summary = "섹션 재생성",
            description = "기존 문서의 특정 섹션만 다시 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재생성 성공"),
            @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음")
    })
    public ResponseEntity<BusinessPlanDocumentResponse> regenerateSection(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "문서 ID") @PathVariable String documentId,
            @Parameter(description = "섹션 타입") @PathVariable String sectionType
    ) {
        log.info("POST /projects/{}/documents/{}/sections/{}/regenerate", 
                projectId, documentId, sectionType);
        
        BusinessPlanDocument document = orchestrationService.regenerateSection(documentId, sectionType);
        
        return ResponseEntity.ok(BusinessPlanDocumentResponse.from(document));
    }

    /**
     * 최신 문서 조회.
     */
    @GetMapping("/business-plan/latest")
    @Operation(
            summary = "최신 사업계획서 조회",
            description = "프로젝트의 가장 최근에 생성된 사업계획서를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "문서 없음")
    })
    public ResponseEntity<BusinessPlanDocumentResponse> getLatestDocument(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId
    ) {
        log.info("GET /projects/{}/documents/business-plan/latest", projectId);
        
        return orchestrationService.getLatestDocument(projectId)
                .map(doc -> ResponseEntity.ok(BusinessPlanDocumentResponse.from(doc)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 특정 문서 조회.
     */
    @GetMapping("/{documentId}")
    @Operation(
            summary = "문서 조회",
            description = "특정 문서 ID로 사업계획서를 조회합니다."
    )
    public ResponseEntity<BusinessPlanDocumentResponse> getDocument(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "문서 ID") @PathVariable String documentId
    ) {
        log.info("GET /projects/{}/documents/{}", projectId, documentId);
        
        return orchestrationService.getDocument(documentId)
                .filter(doc -> doc.getProjectId().equals(projectId))
                .map(doc -> ResponseEntity.ok(BusinessPlanDocumentResponse.from(doc)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 모든 버전 목록 조회.
     */
    @GetMapping("/business-plan/versions")
    @Operation(
            summary = "문서 버전 목록",
            description = "프로젝트의 모든 사업계획서 버전을 조회합니다."
    )
    public ResponseEntity<List<BusinessPlanDocumentResponse>> getAllVersions(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId
    ) {
        log.info("GET /projects/{}/documents/business-plan/versions", projectId);
        
        List<BusinessPlanDocumentResponse> versions = orchestrationService.getAllVersions(projectId)
                .stream()
                .map(BusinessPlanDocumentResponse::from)
                .toList();
        
        return ResponseEntity.ok(versions);
    }
}

