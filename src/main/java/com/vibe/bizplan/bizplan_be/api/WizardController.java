package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.service.WizardService;
import com.vibe.bizplan.bizplan_be.dto.request.SaveWizardAnswersRequest;
import com.vibe.bizplan.bizplan_be.dto.response.WizardAnswersResponse;
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

import java.util.Map;

/**
 * Wizard REST API 컨트롤러.
 * Wizard 단계별 답변 저장 및 조회 API를 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/wizard")
@RequiredArgsConstructor
@Tag(name = "Wizard", description = "Wizard 단계별 답변 관리 API")
public class WizardController {

    private final WizardService wizardService;

    /**
     * Wizard 답변 저장 (Upsert).
     * 특정 단계의 답변을 저장하거나 업데이트한다.
     * 기존 답변은 유지되며 해당 단계의 답변만 병합된다.
     *
     * @param projectId 프로젝트 ID
     * @param request 답변 저장 요청
     * @return 업데이트된 전체 답변
     */
    @PostMapping("/steps")
    @Operation(summary = "답변 저장", description = "Wizard 특정 단계의 답변을 저장합니다. 기존 답변과 병합됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = WizardAnswersResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    public ResponseEntity<WizardAnswersResponse> saveAnswers(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Valid @RequestBody SaveWizardAnswersRequest request
    ) {
        long startTime = System.currentTimeMillis();
        String stepId = request.stepId();
        int fieldsCount = request.answers() != null ? request.answers().size() : 0;
        
        log.info("[API] POST /projects/{}/wizard/steps 요청 수신 - stepId={}, fieldsCount={}", 
                projectId, stepId, fieldsCount);
        
        try {
            WizardAnswersResponse response = wizardService.saveAnswers(projectId, request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[API] POST /projects/{}/wizard/steps 응답 완료 - stepId={}, completedSteps={}/{}, duration={}ms", 
                    projectId, stepId, response.completedSteps(), response.totalSteps(), duration);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[API] POST /projects/{}/wizard/steps 요청 실패 (400) - stepId={}, error={}, duration={}ms", 
                    projectId, stepId, e.getMessage(), duration);
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[API] POST /projects/{}/wizard/steps 요청 실패 (500) - stepId={}, duration={}ms", 
                    projectId, stepId, duration, e);
            throw e;
        }
    }

    /**
     * Wizard 전체 답변 조회.
     * 프로젝트에 저장된 모든 단계의 답변을 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @return 전체 답변 데이터
     */
    @GetMapping("/answers")
    @Operation(summary = "전체 답변 조회", description = "프로젝트에 저장된 모든 Wizard 답변을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = WizardAnswersResponse.class))),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    public ResponseEntity<WizardAnswersResponse> getAnswers(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId
    ) {
        long startTime = System.currentTimeMillis();
        log.info("[API] GET /projects/{}/wizard/answers 요청 수신", projectId);
        
        try {
            WizardAnswersResponse response = wizardService.getAnswers(projectId);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[API] GET /projects/{}/wizard/answers 응답 완료 - completedSteps={}/{}, duration={}ms", 
                    projectId, response.completedSteps(), response.totalSteps(), duration);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[API] GET /projects/{}/wizard/answers 요청 실패 (404) - error={}, duration={}ms", 
                    projectId, e.getMessage(), duration);
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[API] GET /projects/{}/wizard/answers 요청 실패 (500) - duration={}ms", 
                    projectId, duration, e);
            throw e;
        }
    }

    /**
     * 특정 단계 답변 조회.
     * 지정된 단계의 답변만 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @param stepId 단계 ID
     * @return 해당 단계의 답변
     */
    @GetMapping("/steps/{stepId}")
    @Operation(summary = "단계별 답변 조회", description = "특정 단계의 답변만 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> getStepAnswers(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId,
            @Parameter(description = "단계 ID") @PathVariable String stepId
    ) {
        long startTime = System.currentTimeMillis();
        log.info("[API] GET /projects/{}/wizard/steps/{} 요청 수신", projectId, stepId);
        
        try {
            Map<String, Object> answers = wizardService.getStepAnswers(projectId, stepId);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[API] GET /projects/{}/wizard/steps/{} 응답 완료 - fieldsCount={}, duration={}ms", 
                    projectId, stepId, answers.size(), duration);
            
            return ResponseEntity.ok(answers);
            
        } catch (IllegalArgumentException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[API] GET /projects/{}/wizard/steps/{} 요청 실패 (404) - error={}, duration={}ms", 
                    projectId, stepId, e.getMessage(), duration);
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[API] GET /projects/{}/wizard/steps/{} 요청 실패 (500) - duration={}ms", 
                    projectId, stepId, duration, e);
            throw e;
        }
    }
}

