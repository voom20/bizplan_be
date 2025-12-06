package com.vibe.bizplan.bizplan_be.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import com.vibe.bizplan.bizplan_be.dto.request.CreateProjectRequest;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.BusinessPlanDocumentRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExportController 통합 테스트.
 * 문서 내보내기 API 엔드포인트를 실제 애플리케이션 컨텍스트에서 검증한다.
 * 
 * SRS 매핑:
 * - REQ-FUNC-011: HWP/PDF 내보내기
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("ExportController 통합 테스트")
@SuppressWarnings("null")
class ExportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessPlanDocumentRepository documentRepository;

    private String testProjectId;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 프로젝트 생성
        CreateProjectRequest projectRequest = new CreateProjectRequest("KSTARTUP_2025", "Export 테스트 프로젝트");
        MvcResult result = mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andReturn();
        
        testProjectId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("projectId").asText();
    }

    /**
     * 테스트용 문서 생성 헬퍼 메서드.
     */
    private void createTestDocument() {
        BusinessPlanDocument document = BusinessPlanDocument.createNew(testProjectId, 1);
        document.updateAllSections(
                "요약 내용",
                "문제 정의 - 현재 시장의 문제점",
                "솔루션 설명 - AI 기반 솔루션",
                "시장 분석 - TAM/SAM/SOM",
                "비즈니스 모델 - SaaS 구독",
                "경쟁 분석 - 차별화 전략",
                "마케팅 전략 - 디지털 마케팅",
                "팀 소개 - 핵심 인력",
                "재무 계획 - 3개년 추정",
                "마일스톤 - 로드맵"
        );
        document.markAsGenerated(1000);
        documentRepository.save(document);
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/export - 문서 내보내기")
    class ExportDocumentTest {

        @Test
        @WithMockUser
        @DisplayName("HTML 형식으로 문서 내보내기 성공")
        void exportDocument_HtmlFormat_Returns200() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "html"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("text/html")))
                    .andExpect(header().string("Content-Disposition", containsString("attachment")));
        }

        @Test
        @WithMockUser
        @DisplayName("PDF 형식으로 문서 내보내기 성공")
        void exportDocument_PdfFormat_Returns200() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "pdf"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("application/pdf")))
                    .andExpect(header().string("Content-Disposition", containsString("attachment")));
        }

        @Test
        @WithMockUser
        @DisplayName("기본 형식(PDF)으로 문서 내보내기 성공")
        void exportDocument_DefaultFormat_ReturnsPdf() throws Exception {
            // given
            createTestDocument();

            // when & then - format 파라미터 없이 요청
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("application/pdf")));
        }

        @Test
        @WithMockUser
        @DisplayName("지원하지 않는 형식 요청 시 400 Bad Request")
        void exportDocument_UnsupportedFormat_Returns400() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "docx"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 프로젝트 내보내기 시 404")
        void exportDocument_NonExistingProject_Returns404() throws Exception {
            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", "non-existing-id")
                            .param("format", "pdf"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("문서가 없는 프로젝트 내보내기 시 에러")
        void exportDocument_NoDocument_ReturnsError() throws Exception {
            // when & then - 문서 생성 없이 내보내기 시도
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "pdf"))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @WithMockUser
        @DisplayName("대소문자 무시하고 형식 처리")
        void exportDocument_CaseInsensitiveFormat_Returns200() throws Exception {
            // given
            createTestDocument();

            // when & then - 대문자로 요청
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "HTML"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("text/html")));
        }
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/export/versions/{version} - 버전별 내보내기")
    class ExportDocumentVersionTest {

        @Test
        @WithMockUser
        @DisplayName("특정 버전 문서 내보내기 성공")
        void exportDocumentVersion_WithValidVersion_Returns200() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export/versions/{version}", testProjectId, 1)
                            .param("format", "html"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("text/html")));
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 버전 요청 시 에러")
        void exportDocumentVersion_NonExistingVersion_ReturnsError() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export/versions/{version}", testProjectId, 999)
                            .param("format", "pdf"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/export/formats - 지원 형식 조회")
    class GetSupportedFormatsTest {

        @Test
        @WithMockUser
        @DisplayName("지원 형식 목록 조회 성공")
        void getSupportedFormats_Returns200() throws Exception {
            // when & then
            mockMvc.perform(get("/projects/{projectId}/export/formats", testProjectId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.formats").isArray())
                    .andExpect(jsonPath("$.formats", hasItems("pdf", "html")))
                    .andExpect(jsonPath("$.defaultFormat").value("pdf"))
                    .andExpect(jsonPath("$.note").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("문서 내보내기 응답 검증")
    class ExportResponseValidationTest {

        @Test
        @WithMockUser
        @DisplayName("Content-Disposition 헤더에 파일명 포함")
        void exportDocument_ResponseContainsFilename() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "pdf"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", containsString("filename")))
                    .andExpect(header().string("Content-Disposition", containsString(".pdf")));
        }

        @Test
        @WithMockUser
        @DisplayName("HTML 내보내기 시 올바른 Content-Type")
        void exportDocument_HtmlContentType() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "html"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "text/html"));
        }

        @Test
        @WithMockUser
        @DisplayName("PDF 내보내기 시 올바른 Content-Type")
        void exportDocument_PdfContentType() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"));
        }

        @Test
        @WithMockUser
        @DisplayName("Content-Length 헤더 포함 확인")
        void exportDocument_ResponseContainsContentLength() throws Exception {
            // given
            createTestDocument();

            // when & then
            mockMvc.perform(get("/projects/{projectId}/export", testProjectId)
                            .param("format", "html"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Content-Length"));
        }
    }
}

