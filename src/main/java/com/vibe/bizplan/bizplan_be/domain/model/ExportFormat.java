package com.vibe.bizplan.bizplan_be.domain.model;

/**
 * 문서 내보내기 포맷.
 */
public enum ExportFormat {
    
    /** PDF 형식 */
    PDF("application/pdf", ".pdf"),
    
    /** HTML 형식 (미리보기용) */
    HTML("text/html", ".html");
    
    // HWP는 라이선스 및 구현 복잡도로 인해 MVP에서는 제외
    // HWP("application/x-hwp", ".hwp");
    
    private final String contentType;
    private final String extension;
    
    ExportFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public String getExtension() {
        return extension;
    }
}

