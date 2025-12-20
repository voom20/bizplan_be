package com.vibe.bizplan.bizplan_be.dto.response;

import java.util.List;

/**
 * PMF 설문 질문 응답 DTO.
 */
public record PmfQuestionResponse(
        /** 질문 ID */
        String id,
        
        /** 질문 내용 */
        String question,
        
        /** 질문 유형 (radio, multiselect, textarea) */
        String type,
        
        /** 필수 여부 */
        boolean required,
        
        /** 선택 옵션 목록 */
        List<QuestionOption> options
) {
    /**
     * 질문 선택 옵션.
     */
    public record QuestionOption(
            /** 옵션 값 */
            int value,
            /** 옵션 라벨 */
            String label
    ) {}
}

