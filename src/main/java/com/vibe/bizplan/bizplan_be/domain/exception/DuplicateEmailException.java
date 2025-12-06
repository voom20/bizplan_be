package com.vibe.bizplan.bizplan_be.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 이메일 중복 시 발생하는 예외.
 * HTTP 409 Conflict 상태 코드를 반환한다.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "이미 존재하는 이메일입니다";
    
    public DuplicateEmailException() {
        super(DEFAULT_MESSAGE);
    }
    
    public DuplicateEmailException(String email) {
        super(DEFAULT_MESSAGE + ": " + email);
    }
}

