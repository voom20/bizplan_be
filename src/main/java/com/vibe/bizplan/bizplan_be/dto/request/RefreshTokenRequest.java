package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 갱신 요청 DTO.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        String refreshToken
) {}

