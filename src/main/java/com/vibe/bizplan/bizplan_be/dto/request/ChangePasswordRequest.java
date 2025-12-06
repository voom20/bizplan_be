package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 비밀번호 변경 요청 DTO.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        String currentPassword,

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인은 필수입니다")
        String newPasswordConfirm
) {}

