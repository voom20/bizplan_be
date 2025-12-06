package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 프로필 수정 요청 DTO.
 */
public record UpdateProfileRequest(
        @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
        String name,

        @Size(max = 200, message = "회사명은 200자를 초과할 수 없습니다")
        String companyName,

        @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
        String phone
) {}

