package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.dto.request.CreateProjectRequest;
import com.vibe.bizplan.bizplan_be.dto.response.ProjectResponse;
import com.vibe.bizplan.bizplan_be.dto.response.ProjectResponseMapper;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * ProjectService 단위 테스트.
 * Mockito를 사용하여 의존성을 모킹하고 비즈니스 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 단위 테스트")
@SuppressWarnings("null") // Mockito 모킹의 null-safety 오탐 억제
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TemplateService templateService;

    @Mock
    private ProjectResponseMapper projectResponseMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private ProjectResponse testProjectResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 Project 엔티티 생성
        testProject = Project.builder()
                .id("test-project-id")
                .templateCode(TemplateCode.KSTARTUP_2025)
                .title("테스트 프로젝트")
                .status(ProjectStatus.DRAFT)
                .userId("default-user")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 테스트용 ProjectResponse 생성
        testProjectResponse = new ProjectResponse(
                "test-project-id",  // projectId
                "KSTARTUP_2025",    // templateCode
                "테스트 프로젝트",     // title
                ProjectStatus.DRAFT, // status
                Collections.emptyMap(), // wizardAnswers
                LocalDateTime.now(),   // createdAt
                LocalDateTime.now()    // updatedAt
        );
    }

    @Nested
    @DisplayName("createProject 메서드")
    class CreateProjectTest {

        @Test
        @DisplayName("유효한 요청으로 프로젝트 생성 성공")
        void createProject_WithValidRequest_ReturnsProjectResponse() {
            // given
            CreateProjectRequest request = new CreateProjectRequest("KSTARTUP_2025", "테스트 프로젝트");
            given(templateService.validateTemplateCode("KSTARTUP_2025")).willReturn(TemplateCode.KSTARTUP_2025);
            given(projectRepository.save(any(Project.class))).willReturn(testProject);
            given(projectResponseMapper.toResponse(testProject)).willReturn(testProjectResponse);

            // when
            ProjectResponse result = projectService.createProject(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.templateCode()).isEqualTo("KSTARTUP_2025");
            assertThat(result.title()).isEqualTo("테스트 프로젝트");
            verify(projectRepository).save(any(Project.class));
        }

        @Test
        @DisplayName("제목 없이 프로젝트 생성 성공")
        void createProject_WithoutTitle_ReturnsProjectResponse() {
            // given
            CreateProjectRequest request = new CreateProjectRequest("KSTARTUP_2025", null);
            given(templateService.validateTemplateCode("KSTARTUP_2025")).willReturn(TemplateCode.KSTARTUP_2025);
            given(projectRepository.save(any(Project.class))).willReturn(testProject);
            given(projectResponseMapper.toResponse(testProject)).willReturn(testProjectResponse);

            // when
            ProjectResponse result = projectService.createProject(request);

            // then
            assertThat(result).isNotNull();
            verify(projectRepository).save(any(Project.class));
        }

        @Test
        @DisplayName("null 요청 시 NullPointerException 발생")
        void createProject_WithNullRequest_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> projectService.createProject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("요청 데이터는 필수입니다");
        }

        @Test
        @DisplayName("유효하지 않은 템플릿 코드로 프로젝트 생성 시 예외 발생")
        void createProject_WithInvalidTemplateCode_ThrowsIllegalArgumentException() {
            // given
            CreateProjectRequest request = new CreateProjectRequest("INVALID_CODE", "테스트");
            given(templateService.validateTemplateCode("INVALID_CODE"))
                    .willThrow(new IllegalArgumentException("유효하지 않은 템플릿 코드입니다: INVALID_CODE"));

            // when & then
            assertThatThrownBy(() -> projectService.createProject(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 템플릿 코드");
        }
    }

    @Nested
    @DisplayName("getProject 메서드")
    class GetProjectTest {

        @Test
        @DisplayName("존재하는 프로젝트 ID로 조회 성공")
        void getProject_WithExistingId_ReturnsProjectResponse() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(projectResponseMapper.toResponse(testProject)).willReturn(testProjectResponse);

            // when
            Optional<ProjectResponse> result = projectService.getProject("test-project-id");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().projectId()).isEqualTo("test-project-id");
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 ID로 조회 시 빈 Optional 반환")
        void getProject_WithNonExistingId_ReturnsEmptyOptional() {
            // given
            given(projectRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when
            Optional<ProjectResponse> result = projectService.getProject("non-existing-id");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null ID로 조회 시 NullPointerException 발생")
        void getProject_WithNullId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> projectService.getProject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("프로젝트 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("getMyProjects 메서드")
    class GetMyProjectsTest {

        @Test
        @DisplayName("사용자의 프로젝트 목록 조회 성공")
        void getMyProjects_ReturnsProjectList() {
            // given
            List<Project> projects = List.of(testProject);
            List<ProjectResponse> responses = List.of(testProjectResponse);
            given(projectRepository.findByUserId("default-user")).willReturn(projects);
            given(projectResponseMapper.toResponseList(projects)).willReturn(responses);

            // when
            List<ProjectResponse> result = projectService.getMyProjects();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).projectId()).isEqualTo("test-project-id");
        }

        @Test
        @DisplayName("프로젝트가 없는 경우 빈 목록 반환")
        void getMyProjects_WhenNoProjects_ReturnsEmptyList() {
            // given
            given(projectRepository.findByUserId("default-user")).willReturn(Collections.emptyList());
            given(projectResponseMapper.toResponseList(Collections.emptyList())).willReturn(Collections.emptyList());

            // when
            List<ProjectResponse> result = projectService.getMyProjects();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getProjectEntity 메서드")
    class GetProjectEntityTest {

        @Test
        @DisplayName("존재하는 프로젝트 ID로 엔티티 조회 성공")
        void getProjectEntity_WithExistingId_ReturnsProject() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            Project result = projectService.getProjectEntity("test-project-id");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-project-id");
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 ID로 조회 시 예외 발생")
        void getProjectEntity_WithNonExistingId_ThrowsIllegalArgumentException() {
            // given
            given(projectRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> projectService.getProjectEntity("non-existing-id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("null ID로 조회 시 NullPointerException 발생")
        void getProjectEntity_WithNullId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> projectService.getProjectEntity(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("프로젝트 ID는 필수입니다");
        }
    }
}

