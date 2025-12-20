-- V5: 재무 데이터 저장 테이블 생성
-- 프로젝트별 재무 가정값과 계산 결과를 저장한다.

CREATE TABLE IF NOT EXISTS financial_data (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL UNIQUE,
    assumptions CLOB NOT NULL,
    projection_result CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_financial_data_project 
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_financial_data_project_id ON financial_data(project_id);
