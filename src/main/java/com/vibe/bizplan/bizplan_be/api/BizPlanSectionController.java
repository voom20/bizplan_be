package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.service.BizPlanSectionService;
import com.vibe.bizplan.bizplan_be.dto.request.GenerateSectionRequest;
import com.vibe.bizplan.bizplan_be.dto.request.UpdateSectionRequest;
import com.vibe.bizplan.bizplan_be.dto.response.BizPlanSectionResponse;
import com.vibe.bizplan.bizplan_be.dto.response.BizPlanSectionsListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사업계획서 섹션 관리 REST API 컨트롤러.
 * AI를 통한 섹션 생성 및 CRUD 기능을 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/bizplan/sections")
@RequiredArgsConstructor
@Tag(name = "BizPlan Sections", description = "사업계획서 섹션 AI 생성 및 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class BizPlanSectionController {

    private final BizPlanSectionService sectionService;

    /**
     * AI를 통해 특정 섹션 생성.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     * @param request     생성 요청 (선택)
     * @return 생성된 섹션 응답
     */
    @PostMapping("/{sectionType}/generate")
    @Operation(
            summary = "AI 섹션 생성",
            description = "AI Engine을 통해 사업계획서 특정 섹션을 생성합니다. " +
                    "Wizard 답변을 기반으로 내용을 생성하며, 이전 섹션의 내용도 참고합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = BizPlanSectionResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 섹션 타입"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "AI 엔진 오류")
    })
    public ResponseEntity<BizPlanSectionResponse> generateSection(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "섹션 타입 (executive_summary, problem_definition 등)")
            @PathVariable String sectionType,
            @Valid @RequestBody(required = false) GenerateSectionRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("POST /projects/{}/bizplan/sections/{}/generate - userId={}",
                projectId, sectionType, user != null ? user.getId() : "anonymous");

        BizPlanSectionResponse response = sectionService.generateSection(projectId, sectionType, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 프로젝트의 모든 섹션 목록 조회.
     *
     * @param projectId 프로젝트 ID
     * @return 섹션 목록
     */
    @GetMapping
    @Operation(
            summary = "섹션 목록 조회",
            description = "프로젝트에 저장된 모든 사업계획서 섹션 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BizPlanSectionsListResponse.class))),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    public ResponseEntity<BizPlanSectionsListResponse> getSections(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId
    ) {
        log.info("GET /projects/{}/bizplan/sections", projectId);

        BizPlanSectionsListResponse response = sectionService.getSections(projectId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 섹션 조회.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     * @return 섹션 응답
     */
    @GetMapping("/{sectionType}")
    @Operation(
            summary = "섹션 조회",
            description = "특정 섹션의 내용을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BizPlanSectionResponse.class))),
            @ApiResponse(responseCode = "404", description = "섹션을 찾을 수 없음")
    })
    public ResponseEntity<BizPlanSectionResponse> getSection(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "섹션 타입") @PathVariable String sectionType
    ) {
        log.info("GET /projects/{}/bizplan/sections/{}", projectId, sectionType);

        return sectionService.getSection(projectId, sectionType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 섹션 내용 수동 업데이트.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     * @param request     업데이트 요청
     * @return 업데이트된 섹션 응답
     */
    @PutMapping("/{sectionType}")
    @Operation(
            summary = "섹션 수정",
            description = "섹션의 제목과 내용을 수동으로 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = BizPlanSectionResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    public ResponseEntity<BizPlanSectionResponse> updateSection(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "섹션 타입") @PathVariable String sectionType,
            @Valid @RequestBody UpdateSectionRequest request
    ) {
        log.info("PUT /projects/{}/bizplan/sections/{}", projectId, sectionType);

        BizPlanSectionResponse response = sectionService.updateSection(projectId, sectionType, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 섹션 삭제.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     * @return 204 No Content
     */
    @DeleteMapping("/{sectionType}")
    @Operation(
            summary = "섹션 삭제",
            description = "특정 섹션을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteSection(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "섹션 타입") @PathVariable String sectionType
    ) {
        log.info("DELETE /projects/{}/bizplan/sections/{}", projectId, sectionType);

        sectionService.deleteSection(projectId, sectionType);
        return ResponseEntity.noContent().build();
    }
}

