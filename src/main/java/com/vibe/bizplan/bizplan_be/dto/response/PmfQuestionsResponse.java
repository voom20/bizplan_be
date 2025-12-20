package com.vibe.bizplan.bizplan_be.dto.response;

import java.util.List;

/**
 * PMF 설문 질문 목록 응답 DTO.
 */
public record PmfQuestionsResponse(
        /** 질문 목록 */
        List<PmfQuestionResponse> questions
) {
}

