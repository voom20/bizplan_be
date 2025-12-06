package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.service.FinancialCalculationService;
import com.vibe.bizplan.bizplan_be.dto.request.FinancialAssumptionsRequest;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 재무 추정 REST API 컨트롤러.
 * 재무 추정 및 유닛 이코노믹스 계산 API를 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/financials")
@RequiredArgsConstructor
@Tag(name = "Financials", description = "재무 추정 및 유닛 이코노믹스 API")
public class FinancialController {

    private final FinancialCalculationService financialCalculationService;

    /**
     * 재무 추정 생성.
     * 핵심 변수를 입력받아 3년치 월별 손익과 유닛 이코노믹스를 계산한다.
     *
     * @param projectId 프로젝트 ID
     * @param request 재무 가정 변수
     * @return 재무 추정 결과 (월별 PL, 연간 요약, 유닛 이코노믹스)
     */
    @PostMapping(":generate")
    @Operation(
            summary = "재무 추정 생성",
            description = "핵심 변수를 기반으로 3년치 월별 손익계산서와 유닛 이코노믹스를 계산합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계산 성공",
                    content = @Content(schema = @Schema(implementation = FinancialProjectionResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 변수")
    })
    public ResponseEntity<FinancialProjectionResponse> generateFinancials(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Valid @RequestBody FinancialAssumptionsRequest request
    ) {
        log.info("POST /projects/{}/financials:generate - initialCapital={}, ARPU={}, CAC={}",
                projectId, request.initialCapital(), request.averageRevenuePerUser(), request.customerAcquisitionCost());
        
        FinancialProjectionResponse response = financialCalculationService.generateProjection(projectId, request);
        
        log.info("재무 추정 완료: months={}, BEP={}개월",
                request.projectionMonths(), response.unitEconomics().breakEvenMonth());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 재무 추정 미리보기 (프로젝트 미연동).
     * 프로젝트 저장 없이 계산 결과만 반환한다.
     *
     * @param request 재무 가정 변수
     * @return 재무 추정 결과
     */
    @PostMapping("/preview")
    @Operation(
            summary = "재무 추정 미리보기",
            description = "프로젝트에 저장하지 않고 재무 추정 결과만 미리 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계산 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 변수")
    })
    public ResponseEntity<FinancialProjectionResponse> previewFinancials(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Valid @RequestBody FinancialAssumptionsRequest request
    ) {
        log.info("POST /projects/{}/financials/preview", projectId);
        
        // 미리보기는 projectId를 "preview"로 설정
        FinancialProjectionResponse response = financialCalculationService.generateProjection("preview-" + projectId, request);
        
        return ResponseEntity.ok(response);
    }
}

