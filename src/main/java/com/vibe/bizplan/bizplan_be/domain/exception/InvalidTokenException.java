package com.vibe.bizplan.bizplan_be.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * JWT 토큰이 유효하지 않을 때 발생하는 예외.
 * HTTP 401 Unauthorized 상태 코드를 반환한다.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "유효하지 않은 토큰입니다";
    
    public InvalidTokenException() {
        super(DEFAULT_MESSAGE);
    }
    
    public InvalidTokenException(String message) {
        super(message);
    }
}

