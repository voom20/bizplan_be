package com.vibe.bizplan.bizplan_be.domain.exception;

/**
 * 문서를 찾을 수 없을 때 발생하는 예외.
 * HTTP 404 응답으로 변환된다.
 */
public class DocumentNotFoundException extends ResourceNotFoundException {

    public DocumentNotFoundException(String documentId) {
        super("문서", documentId);
    }

    public DocumentNotFoundException(String documentId, String message) {
        super("문서", documentId, message);
    }

    /**
     * 프로젝트에 생성된 문서가 없을 때 사용.
     */
    public static DocumentNotFoundException noDocumentForProject(String projectId) {
        return new DocumentNotFoundException(projectId, 
                "프로젝트에 생성된 사업계획서가 없습니다. 먼저 문서를 생성해주세요.");
    }

    /**
     * 특정 버전의 문서를 찾을 수 없을 때 사용.
     */
    public static DocumentNotFoundException versionNotFound(String projectId, int version) {
        return new DocumentNotFoundException(projectId + ":v" + version, 
                String.format("해당 버전의 문서를 찾을 수 없습니다: v%d", version));
    }
}

