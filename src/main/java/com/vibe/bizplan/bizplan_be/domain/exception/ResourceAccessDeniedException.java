package com.vibe.bizplan.bizplan_be.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 리소스에 대한 접근 권한이 없을 때 발생하는 예외.
 * HTTP 403 Forbidden 상태 코드를 반환한다.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceAccessDeniedException extends RuntimeException {
    
    private final String resourceType;
    private final String resourceId;
    private final String code;
    
    public ResourceAccessDeniedException(String resourceType, String resourceId) {
        super(String.format("%s에 대한 접근 권한이 없습니다: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.code = "AUTHZ_001";
    }
    
    public ResourceAccessDeniedException(String resourceType, String resourceId, String code) {
        super(String.format("%s에 대한 접근 권한이 없습니다: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.code = code;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public String getCode() {
        return code;
    }
}

