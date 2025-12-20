package com.vibe.bizplan.bizplan_be.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * 섹션 생성 요청 DTO.
 * 클라이언트에서 AI 섹션 생성을 요청할 때 사용.
 */
@Schema(description = "사업계획서 섹션 AI 생성 요청")
public record GenerateSectionRequest(

        /** 생성 모드 (easy/expert) */
        @Schema(description = "생성 모드", example = "easy", allowableValues = {"easy", "expert"})
        @Pattern(regexp = "^(easy|expert)$", message = "모드는 'easy' 또는 'expert'만 가능합니다")
        String mode,

        /** 추가 지시사항 */
        @Schema(description = "AI에게 전달할 추가 지시사항", example = "기술적인 내용을 더 강조해주세요")
        String additionalInstructions
) {
    /**
     * 기본값 적용 팩토리 메서드.
     */
    public static GenerateSectionRequest withDefaults() {
        return new GenerateSectionRequest("easy", null);
    }

    /**
     * 모드 반환 (기본값 easy).
     */
    public String getMode() {
        return mode != null ? mode : "easy";
    }
}

