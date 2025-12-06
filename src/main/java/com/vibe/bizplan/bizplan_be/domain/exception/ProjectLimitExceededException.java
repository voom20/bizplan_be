package com.vibe.bizplan.bizplan_be.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 프로젝트 생성 한도를 초과했을 때 발생하는 예외.
 * HTTP 403 Forbidden 상태 코드를 반환한다.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProjectLimitExceededException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "프로젝트 생성 한도를 초과했습니다. 프리미엄으로 업그레이드하세요.";
    private static final String CODE = "AUTHZ_004";
    
    private final int currentCount;
    private final int maxCount;
    
    public ProjectLimitExceededException(int currentCount, int maxCount) {
        super(DEFAULT_MESSAGE);
        this.currentCount = currentCount;
        this.maxCount = maxCount;
    }
    
    public String getCode() {
        return CODE;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public int getMaxCount() {
        return maxCount;
    }
}

