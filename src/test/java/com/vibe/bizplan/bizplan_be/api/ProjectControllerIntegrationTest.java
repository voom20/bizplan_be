package com.vibe.bizplan.bizplan_be.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.dto.request.CreateProjectRequest;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController 통합 테스트.
 * 실제 애플리케이션 컨텍스트에서 API 엔드포인트를 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("ProjectController 통합 테스트")
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /projects - 프로젝트 생성")
    class CreateProjectTest {

        @Test
        @WithMockUser
        @DisplayName("유효한 요청으로 프로젝트 생성 성공 - 201 Created")
        void createProject_WithValidRequest_Returns201() throws Exception {
            // given
            CreateProjectRequest request = new CreateProjectRequest("KSTARTUP_2025", "테스트 프로젝트");

            // when & then
            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.projectId").isNotEmpty())
                    .andExpect(jsonPath("$.templateCode").value("KSTARTUP_2025"))
                    .andExpect(jsonPath("$.title").value("테스트 프로젝트"))
                    .andExpect(jsonPath("$.status").value("DRAFT"));
        }

        @Test
        @WithMockUser
        @DisplayName("제목 없이 프로젝트 생성 성공")
        void createProject_WithoutTitle_Returns201() throws Exception {
            // given
            CreateProjectRequest request = new CreateProjectRequest("BANK_LOAN_2025", null);

            // when & then
            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.templateCode").value("BANK_LOAN_2025"))
                    .andExpect(jsonPath("$.title").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("유효하지 않은 템플릿 코드로 프로젝트 생성 시 400 Bad Request")
        void createProject_WithInvalidTemplateCode_Returns400() throws Exception {
            // given
            CreateProjectRequest request = new CreateProjectRequest("INVALID_CODE", "테스트");

            // when & then
            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("템플릿 코드 누락 시 400 Bad Request")
        void createProject_WithoutTemplateCode_Returns400() throws Exception {
            // given
            String requestJson = "{\"title\": \"테스트\"}";

            // when & then
            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /projects/{projectId} - 프로젝트 조회")
    class GetProjectTest {

        @Test
        @WithMockUser
        @DisplayName("존재하는 프로젝트 조회 성공 - 200 OK")
        void getProject_WithExistingId_Returns200() throws Exception {
            // given: 프로젝트 먼저 생성
            CreateProjectRequest request = new CreateProjectRequest("KSTARTUP_2025", "조회 테스트");
            MvcResult createResult = mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            
            String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                    .get("projectId").asText();

            // when & then
            mockMvc.perform(get("/projects/{projectId}", projectId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectId").value(projectId))
                    .andExpect(jsonPath("$.title").value("조회 테스트"));
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 프로젝트 조회 시 404 Not Found")
        void getProject_WithNonExistingId_Returns404() throws Exception {
            // when & then
            mockMvc.perform(get("/projects/{projectId}", "non-existing-uuid"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /projects - 프로젝트 목록 조회")
    class GetMyProjectsTest {

        @Test
        @WithMockUser
        @DisplayName("프로젝트 목록 조회 성공 - 200 OK")
        void getMyProjects_Returns200() throws Exception {
            // given: 프로젝트 2개 생성
            mockMvc.perform(post("/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new CreateProjectRequest("KSTARTUP_2025", "프로젝트 1"))));
            
            mockMvc.perform(post("/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new CreateProjectRequest("BANK_LOAN_2025", "프로젝트 2"))));

            // when & then
            mockMvc.perform(get("/projects"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
        }

        @Test
        @WithMockUser
        @DisplayName("프로젝트가 없는 경우 빈 배열 반환")
        void getMyProjects_WhenNoProjects_ReturnsEmptyArray() throws Exception {
            // when & then (트랜잭션 롤백으로 이전 테스트 데이터 없음)
            mockMvc.perform(get("/projects"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("인증 테스트 (MVP - 인증 생략)")
    class AuthenticationTest {

        /**
         * MVP 단계에서는 모든 요청이 인증 없이 허용됨.
         * 향후 인증 구현 시 이 테스트들을 401/403 검증으로 변경 필요.
         */

        @Test
        @DisplayName("MVP: 인증 없이도 프로젝트 생성 가능")
        void createProject_WithoutAuth_AllowedInMVP() throws Exception {
            // given
            CreateProjectRequest request = new CreateProjectRequest("KSTARTUP_2025", "테스트");

            // when & then: MVP에서는 인증 없이도 허용
            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("MVP: 인증 없이도 프로젝트 조회 가능 (404는 정상)")
        void getProject_WithoutAuth_AllowedInMVP() throws Exception {
            // when & then: MVP에서는 인증 없이도 허용 (존재하지 않는 ID이므로 404)
            mockMvc.perform(get("/projects/non-existing-uuid"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}

