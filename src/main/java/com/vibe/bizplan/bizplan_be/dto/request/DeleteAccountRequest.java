package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 회원 탈퇴 요청 DTO.
 */
public record DeleteAccountRequest(
        @NotBlank(message = "비밀번호 확인은 필수입니다")
        String password
) {}

