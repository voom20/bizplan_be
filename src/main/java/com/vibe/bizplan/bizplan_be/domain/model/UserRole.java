package com.vibe.bizplan.bizplan_be.domain.model;

/**
 * 사용자 역할(Role) 정의.
 * Spring Security의 권한 체계와 연동된다.
 */
public enum UserRole {
    /** 일반 사용자 - 기본 가입자 */
    USER("ROLE_USER", "일반 사용자"),
    
    /** 프리미엄 사용자 - 유료 구독자 */
    PREMIUM("ROLE_PREMIUM", "프리미엄 사용자"),
    
    /** 관리자 - 시스템 관리자 */
    ADMIN("ROLE_ADMIN", "관리자");

    private final String authority;
    private final String displayName;

    UserRole(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }

    /**
     * Spring Security GrantedAuthority 용 권한 문자열 반환.
     * 
     * @return ROLE_ 접두사가 붙은 권한 문자열
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * 화면 표시용 이름 반환.
     * 
     * @return 한글 역할명
     */
    public String getDisplayName() {
        return displayName;
    }
}

