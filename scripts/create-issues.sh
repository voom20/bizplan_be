#!/bin/bash
# GitHub Issue 생성 스크립트
# Task 파일들을 GitHub Issue로 자동 발행합니다.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TASKS_DIR="$PROJECT_ROOT/tasks"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 기존 이슈 목록 캐시 (중복 방지용)
EXISTING_ISSUES=""

# 기존 이슈 목록 로드
load_existing_issues() {
    echo -e "${YELLOW}기존 이슈 목록을 확인합니다...${NC}"
    EXISTING_ISSUES=$(gh issue list --state all --limit 500 --json title -q '.[].title')
}

# 이슈 존재 여부 확인
issue_exists() {
    local title="$1"
    if echo "$EXISTING_ISSUES" | grep -qF "$title"; then
        return 0  # 존재함
    else
        return 1  # 존재하지 않음
    fi
}

# YAML에서 필드 추출
extract_yaml_field() {
    local file="$1"
    local field="$2"
    # YAML 블록 내에서 필드 추출 (간단한 방식)
    grep -E "^${field}:" "$file" | head -1 | sed "s/^${field}:[[:space:]]*//" | sed 's/^"//' | sed 's/"$//' | sed "s/^'//" | sed "s/'$//"
}

# 마크다운 파일에서 이슈 생성
create_issue_from_file() {
    local file="$1"
    local filename=$(basename "$file" .md)
    
    # YAML 블록에서 정보 추출
    local task_id=$(extract_yaml_field "$file" "task_id")
    local title=$(extract_yaml_field "$file" "title")
    local type=$(extract_yaml_field "$file" "type")
    
    # task_id가 없으면 파일명 사용
    if [ -z "$task_id" ]; then
        task_id="$filename"
    fi
    
    # title이 없으면 파일 첫 줄의 # 제목 사용
    if [ -z "$title" ]; then
        title=$(head -1 "$file" | sed 's/^#[[:space:]]*//')
    fi
    
    # 이슈 제목 구성
    local issue_title="[${task_id}] ${title}"
    
    # 중복 확인
    if issue_exists "$issue_title"; then
        echo -e "${YELLOW}[SKIP] 이미 존재: ${issue_title}${NC}"
        return 0
    fi
    
    # 라벨 구성
    local labels="Issue Automation"
    
    # type에 따라 추가 라벨
    case "$type" in
        "functional")
            labels="${labels},enhancement"
            ;;
        "non_functional"|"non-functional")
            labels="${labels},enhancement"
            ;;
        *)
            ;;
    esac
    
    echo -e "${GREEN}[CREATE] ${issue_title}${NC}"
    
    # 이슈 생성
    if gh issue create \
        --title "$issue_title" \
        --body-file "$file" \
        --label "$labels"; then
        echo -e "${GREEN}  ✓ 성공적으로 생성됨${NC}"
        # 캐시 업데이트 (새로 생성된 이슈 추가)
        EXISTING_ISSUES="${EXISTING_ISSUES}"$'\n'"${issue_title}"
    else
        echo -e "${RED}  ✗ 생성 실패${NC}"
        return 1
    fi
}

# 메인 실행
main() {
    echo "=========================================="
    echo "GitHub Issue 자동 생성 스크립트"
    echo "=========================================="
    echo ""
    
    # 기존 이슈 로드
    load_existing_issues
    
    # 처리할 파일 수 카운트
    local total_files=0
    local created=0
    local skipped=0
    local failed=0
    
    # functional 폴더 처리
    echo ""
    echo -e "${YELLOW}=== functional/ 폴더 처리 ===${NC}"
    for file in "$TASKS_DIR"/functional/*.md; do
        if [ -f "$file" ]; then
            total_files=$((total_files + 1))
            if create_issue_from_file "$file"; then
                if issue_exists "[$(extract_yaml_field "$file" "task_id")] $(extract_yaml_field "$file" "title")"; then
                    :  # 이미 존재하거나 방금 생성됨
                fi
            fi
        fi
    done
    
    # non-functional 폴더 처리
    echo ""
    echo -e "${YELLOW}=== non-functional/ 폴더 처리 ===${NC}"
    for file in "$TASKS_DIR"/non-functional/*.md; do
        if [ -f "$file" ]; then
            total_files=$((total_files + 1))
            create_issue_from_file "$file"
        fi
    done
    
    echo ""
    echo "=========================================="
    echo "처리 완료"
    echo "=========================================="
}

main "$@"

