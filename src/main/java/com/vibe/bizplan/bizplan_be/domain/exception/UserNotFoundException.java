package com.vibe.bizplan.bizplan_be.domain.exception;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외.
 * ResourceNotFoundException을 상속받아 HTTP 404 Not Found를 반환한다.
 */
public class UserNotFoundException extends ResourceNotFoundException {
    
    private static final String RESOURCE_TYPE = "User";
    
    public UserNotFoundException(String userId) {
        super(RESOURCE_TYPE, userId, "사용자를 찾을 수 없습니다: " + userId);
    }
}

