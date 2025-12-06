-- ==========================================
-- V1: Create Project Table
-- 프로젝트 엔티티 테이블 생성
-- H2 및 MySQL 공통 호환 스크립트
-- ==========================================

CREATE TABLE projects (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    user_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_template_code ON projects(template_code);

