package com.vibe.bizplan.bizplan_be.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.dto.request.SaveWizardAnswersRequest;
import com.vibe.bizplan.bizplan_be.dto.response.WizardAnswersResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * WizardService 단위 테스트.
 * Wizard 답변 저장 및 조회 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WizardService 단위 테스트")
class WizardServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WizardService wizardService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id("test-project-id")
                .templateCode(TemplateCode.KSTARTUP_2025)
                .title("테스트 프로젝트")
                .status(ProjectStatus.DRAFT)
                .userId("default-user")
                .wizardAnswers(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("saveAnswers 메서드")
    class SaveAnswersTest {

        @Test
        @DisplayName("새로운 답변 저장 성공")
        void saveAnswers_WithNewAnswers_ReturnsUpdatedResponse() {
            // given
            Map<String, Object> answers = new HashMap<>();
            answers.put("companyName", "테스트 회사");
            answers.put("businessType", "IT 서비스");
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", answers);
            
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(projectRepository.save(any(Project.class))).willReturn(testProject);

            // when
            WizardAnswersResponse result = wizardService.saveAnswers("test-project-id", request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.projectId()).isEqualTo("test-project-id");
            assertThat(result.completedSteps()).isEqualTo(1);
            assertThat(result.totalSteps()).isEqualTo(12); // KSTARTUP_2025의 totalSteps
            verify(projectRepository).save(any(Project.class));
        }

        @Test
        @DisplayName("기존 답변에 새 답변 병합 성공")
        void saveAnswers_WithExistingAnswers_MergesAnswers() throws Exception {
            // given
            Map<String, Object> existingAnswers = new HashMap<>();
            existingAnswers.put("step1", Map.of("companyName", "기존 회사"));
            testProject.updateWizardAnswers(objectMapper.writeValueAsString(existingAnswers));
            
            Map<String, Object> newAnswers = new HashMap<>();
            newAnswers.put("marketSize", "100억");
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step2", newAnswers);
            
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(projectRepository.save(any(Project.class))).willReturn(testProject);

            // when
            WizardAnswersResponse result = wizardService.saveAnswers("test-project-id", request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.completedSteps()).isEqualTo(2);
            assertThat(result.answers()).containsKey("step1");
            assertThat(result.answers()).containsKey("step2");
        }

        @Test
        @DisplayName("DRAFT 상태에서 저장 시 IN_PROGRESS로 상태 변경")
        void saveAnswers_WhenDraft_ChangesStatusToInProgress() {
            // given
            Map<String, Object> answers = Map.of("field", "value");
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", answers);
            
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(projectRepository.save(any(Project.class))).willReturn(testProject);

            // when
            wizardService.saveAnswers("test-project-id", request);

            // then
            assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("null projectId로 저장 시 NullPointerException 발생")
        void saveAnswers_WithNullProjectId_ThrowsNullPointerException() {
            // given
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", Map.of());

            // when & then
            assertThatThrownBy(() -> wizardService.saveAnswers(null, request))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("프로젝트 ID는 필수입니다");
        }

        @Test
        @DisplayName("null request로 저장 시 NullPointerException 발생")
        void saveAnswers_WithNullRequest_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> wizardService.saveAnswers("test-project-id", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("요청 데이터는 필수입니다");
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 ID로 저장 시 예외 발생")
        void saveAnswers_WithNonExistingProjectId_ThrowsIllegalArgumentException() {
            // given
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", Map.of());
            given(projectRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> wizardService.saveAnswers("non-existing-id", request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getAnswers 메서드")
    class GetAnswersTest {

        @Test
        @DisplayName("저장된 답변 조회 성공")
        void getAnswers_WithExistingAnswers_ReturnsAnswers() throws Exception {
            // given
            Map<String, Object> existingAnswers = new HashMap<>();
            existingAnswers.put("step1", Map.of("companyName", "테스트 회사"));
            existingAnswers.put("step2", Map.of("marketSize", "100억"));
            testProject.updateWizardAnswers(objectMapper.writeValueAsString(existingAnswers));
            
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            WizardAnswersResponse result = wizardService.getAnswers("test-project-id");

            // then
            assertThat(result).isNotNull();
            assertThat(result.projectId()).isEqualTo("test-project-id");
            assertThat(result.completedSteps()).isEqualTo(2);
            assertThat(result.answers()).containsKey("step1");
            assertThat(result.answers()).containsKey("step2");
        }

        @Test
        @DisplayName("답변이 없는 경우 빈 Map 반환")
        void getAnswers_WithNoAnswers_ReturnsEmptyMap() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            WizardAnswersResponse result = wizardService.getAnswers("test-project-id");

            // then
            assertThat(result).isNotNull();
            assertThat(result.answers()).isEmpty();
            assertThat(result.completedSteps()).isEqualTo(0);
        }

        @Test
        @DisplayName("null projectId로 조회 시 NullPointerException 발생")
        void getAnswers_WithNullProjectId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> wizardService.getAnswers(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("프로젝트 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("getStepAnswers 메서드")
    class GetStepAnswersTest {

        @Test
        @DisplayName("특정 단계의 답변 조회 성공")
        void getStepAnswers_WithExistingStep_ReturnsStepAnswers() throws Exception {
            // given
            Map<String, Object> step1Answers = new HashMap<>();
            step1Answers.put("companyName", "테스트 회사");
            step1Answers.put("businessType", "IT 서비스");
            
            Map<String, Object> allAnswers = new HashMap<>();
            allAnswers.put("step1", step1Answers);
            testProject.updateWizardAnswers(objectMapper.writeValueAsString(allAnswers));
            
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            Map<String, Object> result = wizardService.getStepAnswers("test-project-id", "step1");

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).containsKey("companyName");
            assertThat(result.get("companyName")).isEqualTo("테스트 회사");
        }

        @Test
        @DisplayName("존재하지 않는 단계 조회 시 빈 Map 반환")
        void getStepAnswers_WithNonExistingStep_ReturnsEmptyMap() throws Exception {
            // given
            Map<String, Object> allAnswers = new HashMap<>();
            allAnswers.put("step1", Map.of("field", "value"));
            testProject.updateWizardAnswers(objectMapper.writeValueAsString(allAnswers));
            
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            Map<String, Object> result = wizardService.getStepAnswers("test-project-id", "step99");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null stepId로 조회 시 NullPointerException 발생")
        void getStepAnswers_WithNullStepId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> wizardService.getStepAnswers("test-project-id", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("단계 ID는 필수입니다");
        }
    }
}

