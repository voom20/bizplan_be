package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.domain.entity.User;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 응답 DTO.
 */
public record UserProfileResponse(
        String id,
        String email,
        String name,
        String companyName,
        String phone,
        String role,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    /**
     * User 엔티티로부터 응답 DTO 생성.
     *
     * @param user 사용자 엔티티
     * @return UserProfileResponse
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCompanyName(),
                user.getPhone(),
                user.getRole().getDisplayName(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}

