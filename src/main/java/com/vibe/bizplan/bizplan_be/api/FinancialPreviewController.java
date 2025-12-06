package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.service.FinancialCalculationService;
import com.vibe.bizplan.bizplan_be.dto.request.FinancialAssumptionsRequest;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse;
import io.swagger.v3.oas.annotations.Operation;
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
 * 재무 추정 미리보기 REST API 컨트롤러.
 * 프로젝트 연동 없이 재무 추정 미리보기 기능을 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/financials")
@RequiredArgsConstructor
@Tag(name = "Financials Preview", description = "재무 추정 미리보기 API (프로젝트 미연동)")
public class FinancialPreviewController {

    private final FinancialCalculationService financialCalculationService;

    /**
     * 재무 추정 미리보기 (독립형).
     * 프로젝트 저장 없이 계산 결과만 반환한다.
     * 사용자가 프로젝트 생성 전에 재무 추정 결과를 확인할 때 사용한다.
     *
     * @param request 재무 가정 변수
     * @return 재무 추정 결과 (월별 PL, 연간 요약, 유닛 이코노믹스)
     */
    @PostMapping("/preview")
    @Operation(
            summary = "재무 추정 미리보기",
            description = "프로젝트 연동 없이 재무 추정 결과를 미리 확인합니다. 결과는 저장되지 않습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계산 성공",
                    content = @Content(schema = @Schema(implementation = FinancialProjectionResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 변수")
    })
    public ResponseEntity<FinancialProjectionResponse> previewFinancials(
            @Valid @RequestBody FinancialAssumptionsRequest request
    ) {
        log.info("POST /financials/preview - initialCapital={}, ARPU={}, CAC={}",
                request.initialCapital(), request.averageRevenuePerUser(), request.customerAcquisitionCost());
        
        // 미리보기는 projectId를 "preview"로 고정 설정
        FinancialProjectionResponse response = financialCalculationService.generateProjection("preview", request);
        
        log.info("재무 추정 미리보기 완료: months={}, BEP={}개월",
                request.projectionMonths(), response.unitEconomics().breakEvenMonth());
        
        return ResponseEntity.ok(response);
    }
}

