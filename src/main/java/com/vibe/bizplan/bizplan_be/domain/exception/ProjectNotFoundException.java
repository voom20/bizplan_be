package com.vibe.bizplan.bizplan_be.domain.exception;

/**
 * 프로젝트를 찾을 수 없을 때 발생하는 예외.
 * HTTP 404 응답으로 변환된다.
 */
public class ProjectNotFoundException extends ResourceNotFoundException {

    public ProjectNotFoundException(String projectId) {
        super("프로젝트", projectId);
    }

    public ProjectNotFoundException(String projectId, String message) {
        super("프로젝트", projectId, message);
    }
}

