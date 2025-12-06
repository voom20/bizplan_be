package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.exception.DuplicateEmailException;
import com.vibe.bizplan.bizplan_be.domain.exception.InvalidCredentialsException;
import com.vibe.bizplan.bizplan_be.domain.exception.InvalidTokenException;
import com.vibe.bizplan.bizplan_be.domain.exception.PasswordMismatchException;
import com.vibe.bizplan.bizplan_be.domain.exception.ProjectLimitExceededException;
import com.vibe.bizplan.bizplan_be.domain.exception.ResourceAccessDeniedException;
import com.vibe.bizplan.bizplan_be.domain.exception.ResourceNotFoundException;
import com.vibe.bizplan.bizplan_be.domain.exception.WizardIncompleteException;
import org.springframework.security.access.AccessDeniedException;
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
     * 리소스 미존재 예외 처리.
     * 프로젝트, 문서 등 리소스를 찾을 수 없을 때 404를 반환한다.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("리소스 미존재: {} - {}", ex.getResourceType(), ex.getResourceId());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Wizard 미완료 예외 처리.
     * Wizard가 완료되지 않은 상태에서 문서 생성 시도 시 400을 반환한다.
     */
    @ExceptionHandler(WizardIncompleteException.class)
    public ResponseEntity<ErrorResponse> handleWizardIncompleteException(WizardIncompleteException ex) {
        log.warn("Wizard 미완료: projectId={}, completedSteps={}/{}", 
                ex.getProjectId(), ex.getCompletedSteps(), ex.getRequiredSteps());
        
        Map<String, String> details = new HashMap<>();
        details.put("projectId", ex.getProjectId());
        details.put("completedSteps", String.valueOf(ex.getCompletedSteps()));
        details.put("requiredSteps", String.valueOf(ex.getRequiredSteps()));
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Wizard Incomplete",
                ex.getMessage(),
                details
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 이메일 중복 예외 처리.
     * 회원가입 시 이메일이 이미 존재하는 경우 409를 반환한다.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException ex) {
        log.warn("이메일 중복: {}", ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        details.put("code", "AUTH_001");
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                details
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 인증 실패 예외 처리.
     * 로그인 실패 시 401을 반환한다.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        log.warn("인증 실패: {}", ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        details.put("code", "AUTH_002");
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                details
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 토큰 유효하지 않음 예외 처리.
     * JWT 토큰이 유효하지 않은 경우 401을 반환한다.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("토큰 유효하지 않음: {}", ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        details.put("code", "AUTH_004");
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                details
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 비밀번호 불일치 예외 처리.
     * 비밀번호 변경/탈퇴 시 비밀번호가 일치하지 않는 경우 400을 반환한다.
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatchException(PasswordMismatchException ex) {
        log.warn("비밀번호 불일치: {}", ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        details.put("code", ex.getCode());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                details
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 리소스 접근 거부 예외 처리.
     * 리소스에 대한 접근 권한이 없는 경우 403을 반환한다.
     */
    @ExceptionHandler(ResourceAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessDeniedException(ResourceAccessDeniedException ex) {
        log.warn("리소스 접근 거부: {} - {}", ex.getResourceType(), ex.getResourceId());
        
        Map<String, String> details = new HashMap<>();
        details.put("code", ex.getCode());
        details.put("resourceType", ex.getResourceType());
        details.put("resourceId", ex.getResourceId());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                details
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 프로젝트 생성 한도 초과 예외 처리.
     * 무료 사용자가 최대 프로젝트 개수를 초과한 경우 403을 반환한다.
     */
    @ExceptionHandler(ProjectLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleProjectLimitExceededException(ProjectLimitExceededException ex) {
        log.warn("프로젝트 생성 한도 초과: current={}, max={}", ex.getCurrentCount(), ex.getMaxCount());
        
        Map<String, String> details = new HashMap<>();
        details.put("code", ex.getCode());
        details.put("currentCount", String.valueOf(ex.getCurrentCount()));
        details.put("maxCount", String.valueOf(ex.getMaxCount()));
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                details
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Spring Security AccessDeniedException 처리.
     * @PreAuthorize 등으로 접근이 거부된 경우 403을 반환한다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("접근 거부: {}", ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        details.put("code", "AUTHZ_001");
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "접근 권한이 없습니다",
                details
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
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

