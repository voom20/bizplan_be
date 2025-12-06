package com.vibe.bizplan.bizplan_be.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * API 컨트롤러 로깅 Aspect.
 * 컨트롤러 메서드의 실행 시간과 요청/응답을 자동으로 로깅한다.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * api 패키지 내의 모든 컨트롤러 메서드를 대상으로 함.
     */
    @Pointcut("execution(* com.vibe.bizplan.bizplan_be.api.*Controller.*(..))")
    public void controllerMethods() {}

    /**
     * 컨트롤러 메서드 실행 시간 및 결과 로깅.
     *
     * @param joinPoint 조인포인트
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("controllerMethods()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = formatArgs(joinPoint.getArgs());
        
        long startTime = System.currentTimeMillis();
        log.info("[API] {}.{} 요청 수신 - args={}", className, methodName, args);
        
        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            String status = extractStatus(result);
            log.info("[API] {}.{} 응답 완료 ({}) - duration={}ms", 
                    className, methodName, status, duration);
            
            return result;
            
        } catch (IllegalArgumentException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[API] {}.{} 요청 실패 (400) - error={}, duration={}ms", 
                    className, methodName, e.getMessage(), duration);
            throw e;
            
        } catch (IllegalStateException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[API] {}.{} 요청 실패 (비즈니스 오류) - error={}, duration={}ms", 
                    className, methodName, e.getMessage(), duration);
            throw e;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[API] {}.{} 요청 실패 (500) - duration={}ms", 
                    className, methodName, duration, e);
            throw e;
        }
    }

    /**
     * 메서드 인자를 로깅용 문자열로 포맷팅.
     * 민감한 정보 노출을 방지하기 위해 간략화.
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .map(this::formatArg)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 개별 인자 포맷팅.
     */
    private String formatArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        // String이나 원시 타입은 그대로 출력 (길이 제한)
        if (arg instanceof String str) {
            return str.length() > 50 ? str.substring(0, 50) + "..." : str;
        }
        if (arg instanceof Number || arg instanceof Boolean) {
            return arg.toString();
        }
        // 복잡한 객체는 클래스명만 출력
        return arg.getClass().getSimpleName();
    }

    /**
     * ResponseEntity에서 HTTP 상태 추출.
     */
    private String extractStatus(Object result) {
        if (result instanceof ResponseEntity<?> response) {
            return String.valueOf(response.getStatusCode().value());
        }
        return "OK";
    }
}

