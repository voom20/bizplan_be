package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * PMF 설문 결과 제출 요청 DTO.
 */
public record PmfSubmitRequest(
        /** 설문 답변 목록 */
        @NotEmpty(message = "답변 목록은 필수입니다")
        List<PmfAnswerRequest> answers
) {
    /**
     * 개별 답변.
     */
    public record PmfAnswerRequest(
            /** 질문 ID */
            String questionId,
            /** 답변 값 (단일 값 또는 배열) */
            Object value
    ) {}
}

