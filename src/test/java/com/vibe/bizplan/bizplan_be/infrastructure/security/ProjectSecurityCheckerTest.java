package com.vibe.bizplan.bizplan_be.infrastructure.security;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.domain.model.UserRole;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ProjectSecurityChecker 단위 테스트.
 * 프로젝트 접근 권한 및 생성 권한 검증 로직을 테스트한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectSecurityChecker 단위 테스트")
@SuppressWarnings("null")
class ProjectSecurityCheckerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProjectSecurityChecker projectSecurityChecker;

    private User testUser;
    private User adminUser;
    private User premiumUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // 테스트용 일반 사용자 생성
        testUser = User.builder()
                .id("test-user-id")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("테스트 사용자")
                .role(UserRole.USER)
                .build();

        // 테스트용 관리자 생성
        adminUser = User.builder()
                .id("admin-user-id")
                .email("admin@example.com")
                .passwordHash("hashedPassword")
                .name("관리자")
                .role(UserRole.ADMIN)
                .build();

        // 테스트용 프리미엄 사용자 생성
        premiumUser = User.builder()
                .id("premium-user-id")
                .email("premium@example.com")
                .passwordHash("hashedPassword")
                .name("프리미엄 사용자")
                .role(UserRole.PREMIUM)
                .build();

        // 테스트용 프로젝트 생성
        testProject = Project.builder()
                .id("test-project-id")
                .templateCode(TemplateCode.KSTARTUP_2025)
                .title("테스트 프로젝트")
                .status(ProjectStatus.DRAFT)
                .userId("test-user-id")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    /**
     * 인증 정보를 SecurityContext에 설정하는 헬퍼 메서드.
     */
    private void setAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("isOwner 메서드")
    class IsOwnerTest {

        @Test
        @DisplayName("프로젝트 소유자인 경우 true 반환")
        void isOwner_WhenUserIsOwner_ReturnsTrue() {
            // given
            setAuthenticatedUser(testUser);
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            boolean result = projectSecurityChecker.isOwner("test-project-id");

            // then
            assertThat(result).isTrue();
            verify(projectRepository).findById("test-project-id");
        }

        @Test
        @DisplayName("프로젝트 소유자가 아닌 경우 false 반환")
        void isOwner_WhenUserIsNotOwner_ReturnsFalse() {
            // given
            User otherUser = User.builder()
                    .id("other-user-id")
                    .email("other@example.com")
                    .passwordHash("hashedPassword")
                    .role(UserRole.USER)
                    .build();
            setAuthenticatedUser(otherUser);
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            boolean result = projectSecurityChecker.isOwner("test-project-id");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ADMIN 사용자는 모든 프로젝트에 접근 가능")
        void isOwner_WhenUserIsAdmin_ReturnsTrue() {
            // given
            setAuthenticatedUser(adminUser);

            // when
            boolean result = projectSecurityChecker.isOwner("test-project-id");

            // then
            assertThat(result).isTrue();
            // ADMIN은 DB 조회 없이 바로 true 반환
            verify(projectRepository, never()).findById("test-project-id");
        }

        @Test
        @DisplayName("프로젝트가 존재하지 않는 경우 true 반환 (404는 서비스에서 처리)")
        void isOwner_WhenProjectNotFound_ReturnsTrue() {
            // given
            setAuthenticatedUser(testUser);
            given(projectRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when
            boolean result = projectSecurityChecker.isOwner("non-existing-id");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("projectId가 null인 경우 false 반환")
        void isOwner_WhenProjectIdIsNull_ReturnsFalse() {
            // given
            setAuthenticatedUser(testUser);

            // when
            boolean result = projectSecurityChecker.isOwner(null);

            // then
            assertThat(result).isFalse();
            verify(projectRepository, never()).findById(null);
        }

        @Test
        @DisplayName("인증되지 않은 경우 false 반환")
        void isOwner_WhenNotAuthenticated_ReturnsFalse() {
            // given - 인증 정보 없음
            SecurityContextHolder.clearContext();

            // when
            boolean result = projectSecurityChecker.isOwner("test-project-id");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Principal이 User 타입이 아닌 경우 false 반환")
        void isOwner_WhenPrincipalIsNotUser_ReturnsFalse() {
            // given
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken("stringPrincipal", null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // when
            boolean result = projectSecurityChecker.isOwner("test-project-id");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("canCreateProject 메서드")
    class CanCreateProjectTest {

        @Test
        @DisplayName("일반 사용자가 5개 미만 프로젝트 보유 시 true 반환")
        void canCreateProject_WhenUserHasLessThan5Projects_ReturnsTrue() {
            // given
            setAuthenticatedUser(testUser);
            given(projectRepository.countByUserId("test-user-id")).willReturn(3L);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("일반 사용자가 5개 프로젝트 보유 시 false 반환")
        void canCreateProject_WhenUserHas5Projects_ReturnsFalse() {
            // given
            setAuthenticatedUser(testUser);
            given(projectRepository.countByUserId("test-user-id")).willReturn(5L);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("일반 사용자가 5개 초과 프로젝트 보유 시 false 반환")
        void canCreateProject_WhenUserHasMoreThan5Projects_ReturnsFalse() {
            // given
            setAuthenticatedUser(testUser);
            given(projectRepository.countByUserId("test-user-id")).willReturn(10L);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ADMIN 사용자는 프로젝트 개수 제한 없음")
        void canCreateProject_WhenUserIsAdmin_ReturnsTrue() {
            // given
            setAuthenticatedUser(adminUser);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isTrue();
            // ADMIN은 DB 조회 없이 바로 true 반환
            verify(projectRepository, never()).countByUserId("admin-user-id");
        }

        @Test
        @DisplayName("PREMIUM 사용자는 프로젝트 개수 제한 없음")
        void canCreateProject_WhenUserIsPremium_ReturnsTrue() {
            // given
            setAuthenticatedUser(premiumUser);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isTrue();
            verify(projectRepository, never()).countByUserId("premium-user-id");
        }

        @Test
        @DisplayName("인증되지 않은 경우 false 반환")
        void canCreateProject_WhenNotAuthenticated_ReturnsFalse() {
            // given - 인증 정보 없음
            SecurityContextHolder.clearContext();

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Principal이 User 타입이 아닌 경우 false 반환")
        void canCreateProject_WhenPrincipalIsNotUser_ReturnsFalse() {
            // given
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken("stringPrincipal", null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("프로젝트가 0개인 사용자도 생성 가능")
        void canCreateProject_WhenUserHasNoProjects_ReturnsTrue() {
            // given
            setAuthenticatedUser(testUser);
            given(projectRepository.countByUserId("test-user-id")).willReturn(0L);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getCurrentUserId 메서드")
    class GetCurrentUserIdTest {

        @Test
        @DisplayName("인증된 사용자의 ID 반환")
        void getCurrentUserId_WhenAuthenticated_ReturnsUserId() {
            // given
            setAuthenticatedUser(testUser);

            // when
            String result = projectSecurityChecker.getCurrentUserId();

            // then
            assertThat(result).isEqualTo("test-user-id");
        }

        @Test
        @DisplayName("인증되지 않은 경우 null 반환")
        void getCurrentUserId_WhenNotAuthenticated_ReturnsNull() {
            // given - 인증 정보 없음
            SecurityContextHolder.clearContext();

            // when
            String result = projectSecurityChecker.getCurrentUserId();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Principal이 User 타입이 아닌 경우 null 반환")
        void getCurrentUserId_WhenPrincipalIsNotUser_ReturnsNull() {
            // given
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken("stringPrincipal", null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // when
            String result = projectSecurityChecker.getCurrentUserId();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("ADMIN 사용자 ID도 정상 반환")
        void getCurrentUserId_WhenAdmin_ReturnsAdminUserId() {
            // given
            setAuthenticatedUser(adminUser);

            // when
            String result = projectSecurityChecker.getCurrentUserId();

            // then
            assertThat(result).isEqualTo("admin-user-id");
        }
    }
}

