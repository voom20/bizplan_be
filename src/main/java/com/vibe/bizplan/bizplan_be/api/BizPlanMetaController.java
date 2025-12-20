package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.dto.response.SectionTypesListResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.client.AiEngineClient;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.SectionTypesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사업계획서 메타데이터 REST API 컨트롤러.
 * 섹션 타입 목록 등 정적 메타데이터를 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/bizplan")
@RequiredArgsConstructor
@Tag(name = "BizPlan Metadata", description = "사업계획서 메타데이터 API")
public class BizPlanMetaController {

    private final AiEngineClient aiEngineClient;

    /**
     * 사업계획서 섹션 타입 목록 조회.
     *
     * @return 섹션 타입 목록
     */
    @GetMapping("/section-types")
    @Operation(
            summary = "섹션 타입 목록 조회",
            description = "사업계획서에서 지원하는 모든 섹션 타입 목록을 조회합니다. " +
                    "각 섹션의 타입 코드, 이름, 설명을 제공합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SectionTypesListResponse.class))),
            @ApiResponse(responseCode = "500", description = "AI 엔진 오류")
    })
    public ResponseEntity<SectionTypesListResponse> getSectionTypes() {
        log.info("GET /bizplan/section-types");

        SectionTypesResponse aiResponse = aiEngineClient.getSectionTypes();
        SectionTypesListResponse response = SectionTypesListResponse.from(aiResponse);

        log.info("섹션 타입 목록 조회 완료: count={}", response.sectionTypes().size());
        return ResponseEntity.ok(response);
    }
}

