-- =====================================================
-- V6: BizPlan Sections 테이블 생성
-- AI 생성 사업계획서 섹션 저장용 테이블
-- =====================================================

CREATE TABLE IF NOT EXISTS bizplan_sections (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    section_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content CLOB NOT NULL,
    word_count INT DEFAULT 0,
    model_used VARCHAR(100),
    generation_time_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 프로젝트당 섹션 타입은 유일해야 함
    CONSTRAINT uk_project_section UNIQUE (project_id, section_type),
    
    -- 외래 키 제약
    CONSTRAINT fk_bizplan_sections_project 
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_bizplan_sections_project_id ON bizplan_sections(project_id);
CREATE INDEX IF NOT EXISTS idx_bizplan_sections_section_type ON bizplan_sections(section_type);
