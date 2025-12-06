package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.domain.entity.User;

import java.time.LocalDateTime;

/**
 * 회원가입 응답 DTO.
 */
public record SignupResponse(
        String userId,
        String email,
        String name,
        LocalDateTime createdAt
) {
    /**
     * User 엔티티로부터 응답 DTO 생성.
     *
     * @param user 사용자 엔티티
     * @return SignupResponse
     */
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt()
        );
    }
}

