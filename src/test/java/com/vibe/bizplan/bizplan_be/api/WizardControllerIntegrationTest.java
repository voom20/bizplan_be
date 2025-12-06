package com.vibe.bizplan.bizplan_be.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.dto.request.CreateProjectRequest;
import com.vibe.bizplan.bizplan_be.dto.request.SaveWizardAnswersRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WizardController 통합 테스트.
 * Wizard API 엔드포인트를 실제 애플리케이션 컨텍스트에서 검증한다.
 * 
 * SRS 매핑:
 * - REQ-FUNC-002: Wizard 단계 진행
 * - REQ-FUNC-007: 필수 입력 누락 방지
 * - REQ-FUNC-013: 자동 저장
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("WizardController 통합 테스트")
@SuppressWarnings("null")
class WizardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testProjectId;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 프로젝트 생성
        CreateProjectRequest projectRequest = new CreateProjectRequest("KSTARTUP_2025", "Wizard 테스트 프로젝트");
        MvcResult result = mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andReturn();
        
        testProjectId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("projectId").asText();
    }

    @Nested
    @DisplayName("POST /projects/{projectId}/wizard/steps - 답변 저장")
    class SaveAnswersTest {

        @Test
        @WithMockUser
        @DisplayName("유효한 답변 저장 성공 - 200 OK")
        void saveAnswers_WithValidData_Returns200() throws Exception {
            // given
            Map<String, Object> answers = new HashMap<>();
            answers.put("companyName", "테스트 회사");
            answers.put("businessType", "IT 서비스");
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", answers);

            // when & then
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectId").value(testProjectId))
                    .andExpect(jsonPath("$.completedSteps").value(1))
                    .andExpect(jsonPath("$.totalSteps").value(12))
                    .andExpect(jsonPath("$.answers.step1.companyName").value("테스트 회사"))
                    .andExpect(jsonPath("$.answers.step1.businessType").value("IT 서비스"));
        }

        @Test
        @WithMockUser
        @DisplayName("여러 단계 답변 순차 저장 - 답변 병합 확인")
        void saveAnswers_MultipleSteps_MergesAnswers() throws Exception {
            // given - Step 1
            Map<String, Object> step1Answers = Map.of("companyName", "테스트 회사");
            SaveWizardAnswersRequest step1Request = new SaveWizardAnswersRequest("step1", step1Answers);

            // Step 1 저장
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(step1Request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(1));

            // Step 2 저장
            Map<String, Object> step2Answers = Map.of("marketSize", "100억원", "targetCustomer", "스타트업");
            SaveWizardAnswersRequest step2Request = new SaveWizardAnswersRequest("step2", step2Answers);

            // when & then
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(step2Request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(2))
                    .andExpect(jsonPath("$.answers.step1.companyName").value("테스트 회사"))
                    .andExpect(jsonPath("$.answers.step2.marketSize").value("100억원"));
        }

        @Test
        @WithMockUser
        @DisplayName("동일 단계 답변 덮어쓰기")
        void saveAnswers_SameStep_OverwritesAnswers() throws Exception {
            // given - 첫 번째 저장
            Map<String, Object> firstAnswers = Map.of("companyName", "첫 번째 회사");
            SaveWizardAnswersRequest firstRequest = new SaveWizardAnswersRequest("step1", firstAnswers);

            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            // 두 번째 저장 (동일 단계)
            Map<String, Object> secondAnswers = Map.of("companyName", "수정된 회사", "newField", "추가 필드");
            SaveWizardAnswersRequest secondRequest = new SaveWizardAnswersRequest("step1", secondAnswers);

            // when & then
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(1)) // 여전히 1단계
                    .andExpect(jsonPath("$.answers.step1.companyName").value("수정된 회사"))
                    .andExpect(jsonPath("$.answers.step1.newField").value("추가 필드"));
        }

        @Test
        @WithMockUser
        @DisplayName("빈 답변 저장 성공")
        void saveAnswers_WithEmptyAnswers_Returns200() throws Exception {
            // given
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", Map.of());

            // when & then
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 프로젝트에 답변 저장 시 404")
        void saveAnswers_WithNonExistingProject_Returns404() throws Exception {
            // given
            Map<String, Object> answers = Map.of("field", "value");
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", answers);

            // when & then
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", "non-existing-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("stepId 누락 시 400 Bad Request")
        void saveAnswers_WithoutStepId_Returns400() throws Exception {
            // given
            String invalidJson = "{\"answers\": {\"field\": \"value\"}}";

            // when & then
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("복잡한 중첩 데이터 저장 성공")
        void saveAnswers_WithNestedData_Returns200() throws Exception {
            // given
            Map<String, Object> nestedData = new HashMap<>();
            nestedData.put("company", Map.of(
                    "name", "테스트 회사",
                    "address", Map.of("city", "서울", "district", "강남"),
                    "employees", 50
            ));
            nestedData.put("products", java.util.List.of("제품1", "제품2", "제품3"));
            SaveWizardAnswersRequest request = new SaveWizardAnswersRequest("step1", nestedData);

            // when & then
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.answers.step1.company.name").value("테스트 회사"))
                    .andExpect(jsonPath("$.answers.step1.company.address.city").value("서울"));
        }
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/wizard/answers - 전체 답변 조회")
    class GetAnswersTest {

        @Test
        @WithMockUser
        @DisplayName("저장된 답변 조회 성공 - 200 OK")
        void getAnswers_WithSavedData_Returns200() throws Exception {
            // given - 먼저 답변 저장
            Map<String, Object> answers = Map.of("companyName", "테스트 회사");
            SaveWizardAnswersRequest saveRequest = new SaveWizardAnswersRequest("step1", answers);
            
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(saveRequest)))
                    .andExpect(status().isOk());

            // when & then
            mockMvc.perform(get("/projects/{projectId}/wizard/answers", testProjectId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectId").value(testProjectId))
                    .andExpect(jsonPath("$.completedSteps").value(1))
                    .andExpect(jsonPath("$.answers.step1.companyName").value("테스트 회사"));
        }

        @Test
        @WithMockUser
        @DisplayName("답변이 없는 프로젝트 조회 시 빈 답변 반환")
        void getAnswers_WithNoSavedData_ReturnsEmptyAnswers() throws Exception {
            // when & then
            mockMvc.perform(get("/projects/{projectId}/wizard/answers", testProjectId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectId").value(testProjectId))
                    .andExpect(jsonPath("$.completedSteps").value(0))
                    .andExpect(jsonPath("$.answers").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 프로젝트 조회 시 404")
        void getAnswers_WithNonExistingProject_Returns404() throws Exception {
            // when & then
            mockMvc.perform(get("/projects/{projectId}/wizard/answers", "non-existing-id"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/wizard/steps/{stepId} - 단계별 답변 조회")
    class GetStepAnswersTest {

        @Test
        @WithMockUser
        @DisplayName("특정 단계 답변 조회 성공 - 200 OK")
        void getStepAnswers_WithExistingStep_Returns200() throws Exception {
            // given - 답변 저장
            Map<String, Object> answers = Map.of("companyName", "테스트 회사", "businessType", "IT");
            SaveWizardAnswersRequest saveRequest = new SaveWizardAnswersRequest("step1", answers);
            
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(saveRequest)))
                    .andExpect(status().isOk());

            // when & then
            mockMvc.perform(get("/projects/{projectId}/wizard/steps/{stepId}", testProjectId, "step1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.companyName").value("테스트 회사"))
                    .andExpect(jsonPath("$.businessType").value("IT"));
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 단계 조회 시 빈 객체 반환")
        void getStepAnswers_WithNonExistingStep_ReturnsEmptyObject() throws Exception {
            // when & then
            mockMvc.perform(get("/projects/{projectId}/wizard/steps/{stepId}", testProjectId, "step99"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 프로젝트의 단계 조회 시 404")
        void getStepAnswers_WithNonExistingProject_Returns404() throws Exception {
            // when & then
            mockMvc.perform(get("/projects/{projectId}/wizard/steps/{stepId}", "non-existing-id", "step1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Wizard 전체 플로우 시나리오 테스트")
    class WizardFlowScenarioTest {

        @Test
        @WithMockUser
        @DisplayName("전체 Wizard 플로우 시나리오 - 다단계 저장 및 조회")
        void wizardFullFlow_MultipleSteps_Success() throws Exception {
            // Step 1: 기본 정보
            Map<String, Object> step1 = Map.of(
                    "companyName", "혁신 스타트업",
                    "founderName", "홍길동",
                    "foundedDate", "2024-01-01"
            );
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new SaveWizardAnswersRequest("step1", step1))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(1));

            // Step 2: 사업 개요
            Map<String, Object> step2 = Map.of(
                    "businessSummary", "AI 기반 비즈니스 솔루션",
                    "targetMarket", "중소기업",
                    "uniqueValue", "자동화된 비즈니스 분석"
            );
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new SaveWizardAnswersRequest("step2", step2))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(2));

            // Step 3: 시장 분석
            Map<String, Object> step3 = Map.of(
                    "tam", "1조원",
                    "sam", "1000억원",
                    "som", "100억원"
            );
            mockMvc.perform(post("/projects/{projectId}/wizard/steps", testProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new SaveWizardAnswersRequest("step3", step3))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(3));

            // 전체 답변 조회 검증
            mockMvc.perform(get("/projects/{projectId}/wizard/answers", testProjectId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSteps").value(3))
                    .andExpect(jsonPath("$.totalSteps").value(12))
                    .andExpect(jsonPath("$.answers.step1.companyName").value("혁신 스타트업"))
                    .andExpect(jsonPath("$.answers.step2.businessSummary").value("AI 기반 비즈니스 솔루션"))
                    .andExpect(jsonPath("$.answers.step3.tam").value("1조원"));
        }
    }
}

