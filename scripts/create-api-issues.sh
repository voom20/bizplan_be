#!/bin/bash
# ============================================
# GitHub Issue 생성 스크립트
# BE-API-REQUEST.md 기반 API 개발 이슈 등록
# ============================================
# 사용법: ./scripts/create-api-issues.sh
# 사전 요구사항: gh auth login 완료
# ============================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "============================================"
echo " 🚀 BE API 개발 이슈 등록 스크립트"
echo "============================================"

# GitHub 인증 확인
if ! gh auth status &>/dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    echo "다음 명령어로 인증하세요: gh auth login"
    exit 1
fi

echo -e "${GREEN}✅ GitHub 인증 확인 완료${NC}"
echo ""

# 프로젝트 디렉토리
TASKS_DIR="tasks/functional"

# 이슈 생성 함수
create_issue() {
    local file=$1
    local title=$2
    local labels=$3
    
    if [ -f "$TASKS_DIR/$file" ]; then
        echo -e "${YELLOW}📝 이슈 생성 중: $title${NC}"
        gh issue create \
            --title "$title" \
            --body-file "$TASKS_DIR/$file" \
            --label "$labels"
        echo -e "${GREEN}✅ 완료: $title${NC}"
        echo ""
        sleep 1  # Rate limiting 방지
    else
        echo -e "${RED}❌ 파일 없음: $TASKS_DIR/$file${NC}"
    fi
}

echo "============================================"
echo " 🔴 High Priority Issues (프로젝트 수정/삭제)"
echo "============================================"

create_issue "BE-API-001-project-update.md" \
    "[BE-API-001] 프로젝트 수정 API 구현" \
    "priority-high,feature,backend,api"

create_issue "BE-API-002-project-delete.md" \
    "[BE-API-002] 프로젝트 삭제 API 구현" \
    "priority-high,feature,backend,api"

echo "============================================"
echo " 🟠 Medium Priority Issues (위저드/재무)"
echo "============================================"

create_issue "BE-API-003-wizard-steps.md" \
    "[BE-API-003] 위저드 단계 정의 조회 API 구현" \
    "priority-medium,feature,backend,api"

create_issue "BE-API-004-financials-get.md" \
    "[BE-API-004] 재무 데이터 조회 API 구현" \
    "priority-medium,feature,backend,api"

create_issue "BE-API-005-financials-save.md" \
    "[BE-API-005] 재무 데이터 저장 API 구현" \
    "priority-medium,feature,backend,api"

echo "============================================"
echo " 🟡 Low Priority Issues (PMF)"
echo "============================================"

create_issue "BE-API-006-pmf-questions.md" \
    "[BE-API-006] PMF 설문 질문 조회 API 구현" \
    "priority-low,feature,backend,api"

create_issue "BE-API-007-pmf-submit.md" \
    "[BE-API-007] PMF 설문 결과 제출 API 구현" \
    "priority-low,feature,backend,api"

create_issue "BE-API-008-pmf-report.md" \
    "[BE-API-008] PMF 리포트 조회 API 구현" \
    "priority-low,feature,backend,api"

echo "============================================"
echo -e "${GREEN}🎉 모든 이슈가 성공적으로 생성되었습니다!${NC}"
echo "============================================"
echo ""
echo "📋 생성된 이슈 목록 확인:"
gh issue list --limit 10

