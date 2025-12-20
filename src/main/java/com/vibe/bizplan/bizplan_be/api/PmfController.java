package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.service.PmfService;
import com.vibe.bizplan.bizplan_be.dto.request.PmfSubmitRequest;
import com.vibe.bizplan.bizplan_be.dto.response.PmfQuestionsResponse;
import com.vibe.bizplan.bizplan_be.dto.response.PmfReportResponse;
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
import org.springframework.web.bind.annotation.*;

/**
 * PMF(Product-Market Fit) REST API 컨트롤러.
 * PMF 설문 질문 조회, 결과 제출, 리포트 조회 API를 제공한다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "PMF", description = "Product-Market Fit 진단 API")
public class PmfController {

    private final PmfService pmfService;

    /**
     * PMF 설문 질문 목록 조회.
     *
     * @return PMF 질문 목록
     */
    @GetMapping("/pmf/questions")
    @Operation(summary = "PMF 설문 질문 조회", description = "PMF 진단용 설문 질문 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PmfQuestionsResponse.class)))
    })
    public ResponseEntity<PmfQuestionsResponse> getQuestions() {
        log.info("GET /pmf/questions");
        PmfQuestionsResponse response = pmfService.getQuestions();
        return ResponseEntity.ok(response);
    }

    /**
     * PMF 설문 결과 제출 및 분석.
     *
     * @param projectId 프로젝트 ID
     * @param request 설문 답변
     * @return PMF 분석 리포트
     */
    @PostMapping("/projects/{projectId}/pmf/submit")
    @Operation(summary = "PMF 설문 결과 제출", description = "PMF 설문 답변을 제출하고 분석 결과를 반환합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "분석 완료",
                    content = @Content(schema = @Schema(implementation = PmfReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<PmfReportResponse> submitPmf(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Valid @RequestBody PmfSubmitRequest request
    ) {
        log.info("POST /projects/{}/pmf/submit - answers={}", projectId, request.answers().size());
        PmfReportResponse response = pmfService.submitAndAnalyze(projectId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 저장된 PMF 리포트 조회.
     *
     * @param projectId 프로젝트 ID
     * @return PMF 리포트 (없으면 404)
     */
    @GetMapping("/projects/{projectId}/pmf/report")
    @Operation(summary = "PMF 리포트 조회", description = "저장된 PMF 분석 결과를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PmfReportResponse.class))),
            @ApiResponse(responseCode = "404", description = "PMF 리포트 없음")
    })
    public ResponseEntity<PmfReportResponse> getReport(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId
    ) {
        log.info("GET /projects/{}/pmf/report", projectId);
        
        return pmfService.getReport(projectId)
                .map(report -> {
                    log.info("PMF 리포트 조회 완료 - projectId={}, score={}", projectId, report.score());
                    return ResponseEntity.ok(report);
                })
                .orElseGet(() -> {
                    log.info("PMF 리포트 없음 - projectId={}", projectId);
                    return ResponseEntity.notFound().build();
                });
    }
}

