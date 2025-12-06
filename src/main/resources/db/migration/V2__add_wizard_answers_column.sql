-- ==========================================
-- V2: Add wizard_answers column to projects table
-- Wizard 단계별 답변 저장을 위한 JSON 컬럼 추가
-- ==========================================

ALTER TABLE projects ADD COLUMN wizard_answers TEXT;

-- 컬럼 설명 (MySQL에서는 주석으로 대체)
-- wizard_answers: JSON 형태로 Wizard 각 단계의 답변을 Key-Value로 저장
-- 예: {"step1": {"question1": "답변1"}, "step2": {"question2": "답변2"}}

