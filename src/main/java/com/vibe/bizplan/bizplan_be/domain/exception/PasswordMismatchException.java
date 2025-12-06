package com.vibe.bizplan.bizplan_be.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 비밀번호가 일치하지 않을 때 발생하는 예외.
 * HTTP 400 Bad Request 상태 코드를 반환한다.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordMismatchException extends RuntimeException {
    
    private final String code;
    
    public PasswordMismatchException(String message, String code) {
        super(message);
        this.code = code;
    }
    
    public PasswordMismatchException(String message) {
        this(message, "USER_002");
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * 현재 비밀번호 불일치 예외 생성.
     */
    public static PasswordMismatchException currentPasswordMismatch() {
        return new PasswordMismatchException("현재 비밀번호가 일치하지 않습니다", "USER_002");
    }
    
    /**
     * 새 비밀번호 확인 불일치 예외 생성.
     */
    public static PasswordMismatchException newPasswordConfirmMismatch() {
        return new PasswordMismatchException("새 비밀번호가 일치하지 않습니다", "USER_003");
    }
}

