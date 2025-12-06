package com.vibe.bizplan.bizplan_be.infrastructure.security;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.domain.model.UserRole;
import com.vibe.bizplan.bizplan_be.domain.model.UserStatus;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProjectSecurityChecker 통합 테스트.
 * 실제 데이터베이스와 연동하여 보안 검사 로직을 검증한다.
 */
@SpringBootTest
@Transactional
@DisplayName("ProjectSecurityChecker 통합 테스트")
class ProjectSecurityCheckerIntegrationTest {

    @Autowired
    private ProjectSecurityChecker projectSecurityChecker;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;
    private User premiumUser;
    private User otherUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성 및 저장
        testUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .email("testuser@example.com")
                .passwordHash("hashedPassword")
                .name("테스트 사용자")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        adminUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .email("admin@example.com")
                .passwordHash("hashedPassword")
                .name("관리자")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        premiumUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .email("premium@example.com")
                .passwordHash("hashedPassword")
                .name("프리미엄 사용자")
                .role(UserRole.PREMIUM)
                .status(UserStatus.ACTIVE)
                .build());

        otherUser = userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .email("other@example.com")
                .passwordHash("hashedPassword")
                .name("다른 사용자")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        // 테스트 프로젝트 생성 및 저장
        testProject = projectRepository.save(Project.builder()
                .id(UUID.randomUUID().toString())
                .templateCode(TemplateCode.KSTARTUP_2025)
                .title("테스트 프로젝트")
                .status(ProjectStatus.DRAFT)
                .userId(testUser.getId())
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("isOwner 통합 테스트")
    class IsOwnerIntegrationTest {

        @Test
        @DisplayName("프로젝트 소유자가 자신의 프로젝트에 접근 가능")
        void isOwner_WhenOwnerAccessesOwnProject_ReturnsTrue() {
            // given
            setAuthenticatedUser(testUser);

            // when
            boolean result = projectSecurityChecker.isOwner(testProject.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 사용자가 프로젝트에 접근 시 거부됨")
        void isOwner_WhenOtherUserAccessesProject_ReturnsFalse() {
            // given
            setAuthenticatedUser(otherUser);

            // when
            boolean result = projectSecurityChecker.isOwner(testProject.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ADMIN이 다른 사용자의 프로젝트에 접근 가능")
        void isOwner_WhenAdminAccessesAnyProject_ReturnsTrue() {
            // given
            setAuthenticatedUser(adminUser);

            // when
            boolean result = projectSecurityChecker.isOwner(testProject.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 ID로 접근 시 true 반환 (404는 서비스에서 처리)")
        void isOwner_WhenProjectNotExists_ReturnsTrue() {
            // given
            setAuthenticatedUser(testUser);
            String nonExistingProjectId = UUID.randomUUID().toString();

            // when
            boolean result = projectSecurityChecker.isOwner(nonExistingProjectId);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("canCreateProject 통합 테스트")
    class CanCreateProjectIntegrationTest {

        @Test
        @DisplayName("일반 사용자가 프로젝트 5개 미만일 때 생성 가능")
        void canCreateProject_WhenUserHasLessThan5Projects_ReturnsTrue() {
            // given
            setAuthenticatedUser(testUser);

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("일반 사용자가 프로젝트 5개일 때 생성 불가")
        void canCreateProject_WhenUserHas5Projects_ReturnsFalse() {
            // given
            setAuthenticatedUser(testUser);
            
            // 추가로 4개의 프로젝트 생성 (기존 1개 + 4개 = 5개)
            for (int i = 0; i < 4; i++) {
                projectRepository.save(Project.builder()
                        .id(UUID.randomUUID().toString())
                        .templateCode(TemplateCode.KSTARTUP_2025)
                        .title("프로젝트 " + i)
                        .status(ProjectStatus.DRAFT)
                        .userId(testUser.getId())
                        .build());
            }

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ADMIN 사용자는 프로젝트 개수 제한 없음")
        void canCreateProject_WhenAdmin_AlwaysReturnsTrue() {
            // given
            setAuthenticatedUser(adminUser);
            
            // ADMIN도 프로젝트를 많이 가지고 있어도 생성 가능
            for (int i = 0; i < 10; i++) {
                projectRepository.save(Project.builder()
                        .id(UUID.randomUUID().toString())
                        .templateCode(TemplateCode.KSTARTUP_2025)
                        .title("관리자 프로젝트 " + i)
                        .status(ProjectStatus.DRAFT)
                        .userId(adminUser.getId())
                        .build());
            }

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PREMIUM 사용자는 프로젝트 개수 제한 없음")
        void canCreateProject_WhenPremium_AlwaysReturnsTrue() {
            // given
            setAuthenticatedUser(premiumUser);
            
            // PREMIUM도 프로젝트를 많이 가지고 있어도 생성 가능
            for (int i = 0; i < 10; i++) {
                projectRepository.save(Project.builder()
                        .id(UUID.randomUUID().toString())
                        .templateCode(TemplateCode.KSTARTUP_2025)
                        .title("프리미엄 프로젝트 " + i)
                        .status(ProjectStatus.DRAFT)
                        .userId(premiumUser.getId())
                        .build());
            }

            // when
            boolean result = projectSecurityChecker.canCreateProject();

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getCurrentUserId 통합 테스트")
    class GetCurrentUserIdIntegrationTest {

        @Test
        @DisplayName("인증된 사용자의 ID를 반환")
        void getCurrentUserId_WhenAuthenticated_ReturnsUserId() {
            // given
            setAuthenticatedUser(testUser);

            // when
            String result = projectSecurityChecker.getCurrentUserId();

            // then
            assertThat(result).isEqualTo(testUser.getId());
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
    }

    @Nested
    @DisplayName("복합 시나리오 테스트")
    class ComplexScenarioTest {

        @Test
        @DisplayName("사용자가 다른 사용자의 프로젝트 접근 후 자신의 프로젝트 접근")
        void multipleProjectAccess_SwitchBetweenProjects() {
            // given
            Project otherProject = projectRepository.save(Project.builder()
                    .id(UUID.randomUUID().toString())
                    .templateCode(TemplateCode.BANK_LOAN_2025)
                    .title("다른 사용자의 프로젝트")
                    .status(ProjectStatus.DRAFT)
                    .userId(otherUser.getId())
                    .build());

            setAuthenticatedUser(testUser);

            // when & then
            // 다른 사용자의 프로젝트 접근 시도 - 실패해야 함
            assertThat(projectSecurityChecker.isOwner(otherProject.getId())).isFalse();
            
            // 자신의 프로젝트 접근 - 성공해야 함
            assertThat(projectSecurityChecker.isOwner(testProject.getId())).isTrue();
        }

        @Test
        @DisplayName("사용자가 프로젝트 한도 근처에서 생성 시도")
        void projectCreationAtLimit_BoundaryTest() {
            // given
            setAuthenticatedUser(testUser);
            
            // 4개 프로젝트 추가 (기존 1개 포함하여 총 5개)
            for (int i = 0; i < 4; i++) {
                projectRepository.save(Project.builder()
                        .id(UUID.randomUUID().toString())
                        .templateCode(TemplateCode.KSTARTUP_2025)
                        .title("한도 테스트 프로젝트 " + i)
                        .status(ProjectStatus.DRAFT)
                        .userId(testUser.getId())
                        .build());
            }

            // when & then
            // 5개 프로젝트가 있으므로 생성 불가
            assertThat(projectSecurityChecker.canCreateProject()).isFalse();
        }
    }
}

