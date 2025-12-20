package com.vibe.bizplan.bizplan_be.dto.response;

import java.util.List;

/**
 * 위저드 질문 정의 응답 DTO.
 * 위저드 단계 내 개별 질문의 정의 정보를 담는다.
 */
public record WizardQuestionResponse(
        /** 질문 ID */
        String id,
        
        /** 질문 유형 (text, textarea, number, select, multiselect, date, radio) */
        String type,
        
        /** 질문 라벨 */
        String label,
        
        /** 입력 플레이스홀더 */
        String placeholder,
        
        /** 필수 여부 */
        boolean required,
        
        /** 최대 길이 (text, textarea) */
        Integer maxLength,
        
        /** 선택 옵션 목록 (select, multiselect, radio) */
        List<QuestionOption> options
) {
    /**
     * 선택 옵션 정의.
     */
    public record QuestionOption(
            /** 옵션 값 */
            Object value,
            /** 옵션 라벨 */
            String label
    ) {}
}

