-- V5: 재무 데이터 저장 테이블 생성
-- 프로젝트별 재무 가정값과 계산 결과를 저장한다.

CREATE TABLE financial_data (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL UNIQUE,
    assumptions TEXT NOT NULL COMMENT 'JSON: 재무 가정값',
    projection_result TEXT COMMENT 'JSON: 계산된 재무 추정 결과',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_financial_data_project 
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_financial_data_project_id ON financial_data(project_id);

