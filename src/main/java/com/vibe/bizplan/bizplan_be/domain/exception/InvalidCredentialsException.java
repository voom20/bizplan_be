package com.vibe.bizplan.bizplan_be.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 로그인 인증 실패 시 발생하는 예외.
 * HTTP 401 Unauthorized 상태 코드를 반환한다.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다";
    
    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

