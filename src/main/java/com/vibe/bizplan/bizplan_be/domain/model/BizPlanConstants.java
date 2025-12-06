package com.vibe.bizplan.bizplan_be.domain.model;

import java.math.BigDecimal;

/**
 * 비즈플랜 서비스 전역 상수 정의.
 * 매직 넘버를 의미 있는 상수로 관리한다.
 */
public final class BizPlanConstants {

    private BizPlanConstants() {
        // 인스턴스화 방지
    }

    // ==========================================
    // Wizard 관련 상수
    // ==========================================
    
    /** 기본 Wizard 단계 수 (템플릿 정보 없을 때 사용) */
    public static final int DEFAULT_WIZARD_STEPS = 10;

    // ==========================================
    // Financial 관련 상수
    // ==========================================
    
    /** 미리보기용 프로젝트 ID */
    public static final String PREVIEW_PROJECT_ID = "preview";
    
    /** 최대 고객 유지 기간 (개월) - 이탈률 0일 때 사용 */
    public static final int MAX_CUSTOMER_LIFESPAN_MONTHS = 120;
    
    /** 기본 추정 기간 (개월) */
    public static final int DEFAULT_PROJECTION_MONTHS = 36;
    
    /** 기본 변동비율 (30%) */
    public static final BigDecimal DEFAULT_VARIABLE_COST_RATE = BigDecimal.valueOf(0.3);

    // ==========================================
    // User 관련 상수 (MVP)
    // ==========================================
    
    /** MVP용 기본 사용자 ID */
    public static final String DEFAULT_USER_ID = "default-user";
}

