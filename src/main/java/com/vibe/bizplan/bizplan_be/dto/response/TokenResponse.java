package com.vibe.bizplan.bizplan_be.dto.response;

/**
 * JWT 토큰 응답 DTO.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    /**
     * 토큰 응답 생성.
     *
     * @param accessToken Access Token
     * @param refreshToken Refresh Token
     * @param expiresIn Access Token 만료 시간 (밀리초)
     * @return TokenResponse
     */
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}

