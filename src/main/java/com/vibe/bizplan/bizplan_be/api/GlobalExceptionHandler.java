package com.vibe.bizplan.bizplan_be.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러.
 * API 예외를 일관된 형식으로 클라이언트에게 반환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 에러 응답 레코드.
     */
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            Map<String, String> details
    ) {
        public ErrorResponse(int status, String error, String message) {
            this(LocalDateTime.now(), status, error, message, null);
        }
        
        public ErrorResponse(int status, String error, String message, Map<String, String> details) {
            this(LocalDateTime.now(), status, error, message, details);
        }
    }

    /**
     * Validation 예외 처리.
     * @Valid 검증 실패 시 발생하는 예외를 처리한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation 오류: {}", ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "입력값 검증에 실패했습니다",
                details
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리.
     * 비즈니스 로직에서 발생하는 잘못된 인자 예외를 처리한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("잘못된 요청: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 일반 예외 처리.
     * 처리되지 않은 예외를 500 에러로 반환한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("서버 오류 발생", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

