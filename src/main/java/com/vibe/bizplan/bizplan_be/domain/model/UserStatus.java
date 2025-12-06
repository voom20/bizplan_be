package com.vibe.bizplan.bizplan_be.domain.model;

/**
 * 사용자 계정 상태 정의.
 */
public enum UserStatus {
    /** 활성 상태 - 정상 사용 가능 */
    ACTIVE("활성"),
    
    /** 정지 상태 - 관리자에 의해 정지됨 */
    SUSPENDED("정지"),
    
    /** 삭제 상태 - 탈퇴 처리됨 (소프트 삭제) */
    DELETED("삭제");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

