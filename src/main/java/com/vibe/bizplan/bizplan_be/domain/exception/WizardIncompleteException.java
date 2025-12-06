package com.vibe.bizplan.bizplan_be.domain.exception;

/**
 * Wizard가 완료되지 않은 상태에서 문서 생성을 시도할 때 발생하는 예외.
 * HTTP 400 응답으로 변환된다.
 */
public class WizardIncompleteException extends RuntimeException {

    private final String projectId;
    private final int completedSteps;
    private final int requiredSteps;

    public WizardIncompleteException(String projectId) {
        super("Wizard 답변이 없습니다. 먼저 Wizard를 완료해주세요.");
        this.projectId = projectId;
        this.completedSteps = 0;
        this.requiredSteps = 0;
    }

    public WizardIncompleteException(String projectId, int completedSteps, int requiredSteps) {
        super(String.format("Wizard가 완료되지 않았습니다. (완료: %d/%d 단계)", completedSteps, requiredSteps));
        this.projectId = projectId;
        this.completedSteps = completedSteps;
        this.requiredSteps = requiredSteps;
    }

    public String getProjectId() {
        return projectId;
    }

    public int getCompletedSteps() {
        return completedSteps;
    }

    public int getRequiredSteps() {
        return requiredSteps;
    }
}

