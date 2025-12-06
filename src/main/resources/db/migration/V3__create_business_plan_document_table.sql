-- ==========================================
-- V3: Create BusinessPlanDocument Table
-- 사업계획서 문서 저장 테이블
-- ==========================================

CREATE TABLE business_plan_documents (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    
    -- 섹션별 내용 (TEXT 타입으로 긴 텍스트 저장)
    executive_summary TEXT,
    problem_definition TEXT,
    solution TEXT,
    market_analysis TEXT,
    business_model TEXT,
    competitive_analysis TEXT,
    marketing_strategy TEXT,
    team TEXT,
    financial_plan TEXT,
    milestones TEXT,
    
    -- 메타데이터
    total_word_count INT DEFAULT 0,
    generation_time_ms INT DEFAULT 0,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 외래키 (논리적 참조, H2/MySQL 호환)
    CONSTRAINT fk_document_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_documents_project_id ON business_plan_documents(project_id);
CREATE INDEX idx_documents_status ON business_plan_documents(status);
CREATE INDEX idx_documents_version ON business_plan_documents(project_id, version);

