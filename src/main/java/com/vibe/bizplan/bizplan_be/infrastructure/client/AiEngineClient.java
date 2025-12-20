package com.vibe.bizplan.bizplan_be.infrastructure.client;

import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.BizPlanGenerateRequest;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.BizPlanGenerateResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.PmfCriteriaResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.PmfDiagnoseRequest;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.PmfDiagnoseResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.SectionTypesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;

/**
 * AI Engine (Python FastAPI) HTTP 클라이언트.
 * 사업계획서 생성 및 PMF 진단 API를 호출한다.
 */
@Slf4j
@Component
public class AiEngineClient {

    private final RestClient restClient;

    /**
     * AI Engine 클라이언트 생성자.
     *
     * @param aiEngineUrl AI 엔진 URL (null 불가)
     * @param timeoutSeconds 타임아웃 (초)
     */
    public AiEngineClient(
            @Value("${ai.engine.url:http://localhost:8000}") String aiEngineUrl,
            @Value("${ai.engine.timeout:60}") int timeoutSeconds
    ) {
        Objects.requireNonNull(aiEngineUrl, "AI 엔진 URL은 필수입니다");
        this.restClient = RestClient.builder()
                .baseUrl(aiEngineUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        log.info("AI Engine 클라이언트 초기화: url={}, timeout={}s", aiEngineUrl, timeoutSeconds);
    }

    /**
     * 사업계획서 섹션 생성 요청.
     *
     * @param request 생성 요청
     * @return 생성 응답
     * @throws AiEngineException AI 엔진 호출 실패 시
     */
    public BizPlanGenerateResponse generateSection(BizPlanGenerateRequest request) {
        log.info("AI 엔진 섹션 생성 요청: projectId={}, section={}", 
                request.projectId(), request.sectionType());
        
        try {
            @SuppressWarnings("null") // MediaType.APPLICATION_JSON은 항상 non-null
            BizPlanGenerateResponse response = restClient.post()
                    .uri("/api/v1/bizplan/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(BizPlanGenerateResponse.class);
            
            log.info("AI 엔진 섹션 생성 완료: projectId={}, section={}, wordCount={}",
                    request.projectId(), request.sectionType(),
                    response != null && response.section() != null ? response.section().wordCount() : 0);
            
            return response;
            
        } catch (RestClientException e) {
            log.error("AI 엔진 호출 실패: projectId={}, section={}, error={}",
                    request.projectId(), request.sectionType(), e.getMessage());
            throw new AiEngineException("AI 엔진 호출에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * PMF AI 진단 요청.
     *
     * @param request 진단 요청
     * @return 진단 응답
     * @throws AiEngineException AI 엔진 호출 실패 시
     */
    public PmfDiagnoseResponse diagnosePmf(PmfDiagnoseRequest request) {
        log.info("AI 엔진 PMF 진단 요청: projectId={}", request.projectId());
        
        try {
            @SuppressWarnings("null")
            PmfDiagnoseResponse response = restClient.post()
                    .uri("/api/v1/pmf/diagnose")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PmfDiagnoseResponse.class);
            
            log.info("AI 엔진 PMF 진단 완료: projectId={}, score={}, grade={}",
                    request.projectId(),
                    response != null ? response.overallScore() : 0,
                    response != null ? response.scoreGrade() : "unknown");
            
            return response;
            
        } catch (RestClientException e) {
            log.error("AI 엔진 PMF 진단 실패: projectId={}, error={}",
                    request.projectId(), e.getMessage());
            throw new AiEngineException("AI 엔진 PMF 진단에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사업계획서 섹션 타입 목록 조회.
     *
     * @return 섹션 타입 목록
     * @throws AiEngineException AI 엔진 호출 실패 시
     */
    public SectionTypesResponse getSectionTypes() {
        log.info("AI 엔진 섹션 타입 목록 조회");
        
        try {
            @SuppressWarnings("null")
            SectionTypesResponse response = restClient.get()
                    .uri("/api/v1/bizplan/sections")
                    .retrieve()
                    .body(SectionTypesResponse.class);
            
            log.info("AI 엔진 섹션 타입 목록 조회 완료: count={}",
                    response != null && response.sectionTypes() != null ? response.sectionTypes().size() : 0);
            
            return response;
            
        } catch (RestClientException e) {
            log.error("AI 엔진 섹션 타입 목록 조회 실패: error={}", e.getMessage());
            throw new AiEngineException("AI 엔진 섹션 타입 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * PMF 평가 기준 목록 조회.
     *
     * @return PMF 평가 기준 목록
     * @throws AiEngineException AI 엔진 호출 실패 시
     */
    public PmfCriteriaResponse getPmfCriteria() {
        log.info("AI 엔진 PMF 평가 기준 조회");
        
        try {
            @SuppressWarnings("null")
            PmfCriteriaResponse response = restClient.get()
                    .uri("/api/v1/pmf/criteria")
                    .retrieve()
                    .body(PmfCriteriaResponse.class);
            
            log.info("AI 엔진 PMF 평가 기준 조회 완료: count={}",
                    response != null && response.criteria() != null ? response.criteria().size() : 0);
            
            return response;
            
        } catch (RestClientException e) {
            log.error("AI 엔진 PMF 평가 기준 조회 실패: error={}", e.getMessage());
            throw new AiEngineException("AI 엔진 PMF 평가 기준 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * AI 엔진 헬스 체크.
     *
     * @return AI 엔진이 정상이면 true
     */
    public boolean isHealthy() {
        try {
            var response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(HealthResponse.class);
            
            return response != null && "healthy".equals(response.status());
            
        } catch (Exception e) {
            log.warn("AI 엔진 헬스 체크 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 헬스 체크 응답 내부 클래스.
     */
    private record HealthResponse(String status, boolean llmAvailable) {}

    /**
     * AI 엔진 호출 예외.
     */
    public static class AiEngineException extends RuntimeException {
        public AiEngineException(String message) {
            super(message);
        }

        public AiEngineException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

