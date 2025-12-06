package com.vibe.bizplan.bizplan_be.domain.model;

/**
 * 사업계획서 문서 상태 열거형.
 */
public enum DocumentStatus {
    
    /** 생성 중 - AI 엔진에서 생성 진행 중 */
    GENERATING,
    
    /** 초안 - 생성 완료, 편집 가능 */
    DRAFT,
    
    /** 검토 중 - 사용자가 검토 중 */
    REVIEWING,
    
    /** 최종 확정 */
    FINALIZED,
    
    /** 생성 실패 */
    FAILED
}

