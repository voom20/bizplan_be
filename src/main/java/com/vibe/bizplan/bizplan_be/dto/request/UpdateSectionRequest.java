package com.vibe.bizplan.bizplan_be.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 섹션 수정 요청 DTO.
 */
@Schema(description = "사업계획서 섹션 수정 요청")
public record UpdateSectionRequest(

        /** 섹션 제목 */
        @Schema(description = "섹션 제목", example = "사업개요")
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255, message = "제목은 255자 이내여야 합니다")
        String title,

        /** 섹션 내용 */
        @Schema(description = "섹션 내용")
        @NotBlank(message = "내용은 필수입니다")
        String content
) {
}

