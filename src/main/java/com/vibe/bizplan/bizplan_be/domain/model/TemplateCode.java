package com.vibe.bizplan.bizplan_be.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사업계획서 템플릿 코드 열거형.
 * 지원하는 정부지원사업 및 대출용 템플릿 목록을 하드코딩으로 관리한다.
 */
@Getter
@RequiredArgsConstructor
public enum TemplateCode {
    
    /** 예비창업패키지 2025 */
    KSTARTUP_2025("예비창업패키지 2025", "K-Startup 예비창업패키지 지원용 사업계획서", "government"),
    
    /** 초기창업패키지 2025 */
    KSTARTUP_EARLY_2025("초기창업패키지 2025", "K-Startup 초기창업패키지 지원용 사업계획서", "government"),
    
    /** 은행 대출용 사업계획서 */
    BANK_LOAN_2025("은행 대출용 2025", "시중은행 사업자대출 심사용 사업계획서", "bank"),
    
    /** 투자유치용 사업계획서 */
    INVESTOR_PITCH_2025("투자유치용 2025", "VC/엔젤 투자유치용 사업계획서", "investor");
    
    /** 템플릿 표시명 */
    private final String displayName;
    
    /** 템플릿 설명 */
    private final String description;
    
    /** 템플릿 카테고리 (government, bank, investor) */
    private final String category;
}

