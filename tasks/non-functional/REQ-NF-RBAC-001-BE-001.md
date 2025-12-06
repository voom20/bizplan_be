# REQ-NF-RBAC-001-BE-001: 역할 기반 접근 제어 (RBAC)

## 1. 개요
- **목표**: 역할(Role) 기반 접근 제어를 구현하여 사용자 권한에 따른 기능 및 데이터 접근을 제어한다.
- **범위**:
  - 역할 정의 및 권한 매핑
  - 메서드 레벨 보안 (@PreAuthorize, @Secured)
  - 워크스페이스/프로젝트 레벨 데이터 격리
- **Out of Scope**: 세분화된 Permission 시스템, ACL (Post-MVP).

## 2. 상세 요구사항

### 2.1 역할(Role) 정의

| 역할 | 코드 | 설명 | 권한 |
|------|------|------|------|
| 일반 사용자 | `ROLE_USER` | 기본 가입자 | 자신의 프로젝트 CRUD |
| 프리미엄 사용자 | `ROLE_PREMIUM` | 유료 구독자 | USER 권한 + 고급 기능 |
| 관리자 | `ROLE_ADMIN` | 시스템 관리자 | 전체 시스템 관리 |

### 2.2 권한별 기능 매핑

| 기능 | USER | PREMIUM | ADMIN |
|------|:----:|:-------:|:-----:|
| 프로젝트 생성 | ✓ (5개 제한) | ✓ (무제한) | ✓ |
| AI 초안 생성 | ✓ (일 10회) | ✓ (무제한) | ✓ |
| PDF 내보내기 | ✓ | ✓ | ✓ |
| HWP 내보내기 | ✗ | ✓ | ✓ |
| PMF 진단 | ✗ | ✓ | ✓ |
| 사용자 관리 | ✗ | ✗ | ✓ |
| 시스템 설정 | ✗ | ✗ | ✓ |

### 2.3 데이터 격리 (Multi-tenancy)
- **원칙**: 사용자는 자신이 소유한 프로젝트/문서에만 접근 가능
- **구현 방식**: 
  - 모든 쿼리에 `user_id` 조건 자동 추가
  - `@Query` 또는 Hibernate Filter 사용
  
```java
// Repository 레벨 데이터 격리 예시
@Query("SELECT p FROM Project p WHERE p.userId = :userId")
List<Project> findAllByUserId(@Param("userId") String userId);
```

### 2.4 메서드 레벨 보안

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(String userId) { ... }

@PreAuthorize("hasAnyRole('USER', 'PREMIUM', 'ADMIN')")
public Project createProject(ProjectRequest request) { ... }

@PreAuthorize("@projectSecurity.isOwner(#projectId)")
public Project getProject(String projectId) { ... }
```

### 2.5 리소스 소유권 검증

```java
@Component("projectSecurity")
public class ProjectSecurityChecker {
    public boolean isOwner(String projectId) {
        String currentUserId = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        return projectRepository.findById(projectId)
            .map(p -> p.getUserId().equals(currentUserId))
            .orElse(false);
    }
}
```

## 3. 데이터베이스 스키마 변경

### 3.1 users 테이블 역할 컬럼
```sql
ALTER TABLE users 
ADD COLUMN role VARCHAR(20) DEFAULT 'USER' 
COMMENT 'ROLE_USER, ROLE_PREMIUM, ROLE_ADMIN';
```

### 3.2 projects 테이블 소유자 컬럼
```sql
ALTER TABLE projects
ADD COLUMN user_id VARCHAR(36) NOT NULL,
ADD CONSTRAINT fk_project_user 
    FOREIGN KEY (user_id) REFERENCES users(id);
```

## 4. 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| AUTHZ_001 | 접근 권한이 없습니다 | 리소스 접근 권한 부족 |
| AUTHZ_002 | 해당 리소스의 소유자가 아닙니다 | 다른 사용자의 리소스 접근 시도 |
| AUTHZ_003 | 프리미엄 기능입니다 | 무료 사용자가 유료 기능 접근 |
| AUTHZ_004 | 일일 사용 한도를 초과했습니다 | Rate Limit 초과 |

---

```yaml
task_id: "REQ-NF-RBAC-001-BE-001"
title: "역할 기반 접근 제어 (RBAC) 구현"
summary: >
  사용자 역할에 따른 기능 접근 제어와 리소스 소유권 기반 
  데이터 격리를 구현하여 보안 및 Multi-tenancy를 보장한다.
type: "non_functional"
epic: "EPIC_SECURITY"
req_ids: ["REQ-NF-008"]
component: ["backend.security", "backend.domain"]
agent_profile: ["backend"]

category: "security"
labels: ["security:rbac", "security:authorization", "multi-tenancy"]

requirements:
  description: >
    사용자는 자신의 역할에 허용된 기능만 사용할 수 있으며,
    자신이 소유한 데이터에만 접근할 수 있어야 한다.
  kpis:
    - "타 사용자 데이터 접근 시도 100% 차단"
    - "권한 우회 취약점 0건"

steps_hint:
  - "@EnableMethodSecurity(prePostEnabled = true) 활성화"
  - "UserRole enum 정의 (USER, PREMIUM, ADMIN)"
  - "User 엔티티에 roles 필드 추가"
  - "ProjectSecurityChecker: 리소스 소유권 검증 빈"
  - "ProjectRepository: user_id 기반 쿼리 메서드 추가"
  - "Flyway 마이그레이션: projects.user_id 컬럼 추가"
  - "서비스 레이어: @PreAuthorize 어노테이션 적용"

preconditions:
  - "REQ-NF-AUTH-001-BE-001이 완료되어야 한다."
  - "User 엔티티가 정의되어 있어야 한다."

postconditions:
  - "USER 권한 사용자는 자신의 프로젝트만 조회/수정 가능"
  - "ADMIN 권한 사용자는 모든 리소스에 접근 가능"
  - "타 사용자의 리소스 접근 시 403 Forbidden 반환"

dependencies: ["REQ-NF-AUTH-001-BE-001", "REQ-FUNC-USER-BE-001"]

acceptance_criteria:
  - given: "USER 권한 사용자가 자신의 프로젝트를 조회하면"
    when: "GET /api/v1/projects/{id} 호출"
    then: "200 OK와 프로젝트 정보가 반환된다"
  - given: "USER 권한 사용자가 다른 사용자의 프로젝트를 조회하면"
    when: "GET /api/v1/projects/{other_id} 호출"
    then: "403 Forbidden이 반환된다"
  - given: "USER 권한 사용자가 ADMIN API를 호출하면"
    when: "GET /api/v1/admin/users 호출"
    then: "403 Forbidden이 반환된다"
  - given: "ADMIN 권한 사용자가 다른 사용자의 프로젝트를 조회하면"
    when: "GET /api/v1/projects/{other_id} 호출"
    then: "200 OK와 프로젝트 정보가 반환된다"
```

