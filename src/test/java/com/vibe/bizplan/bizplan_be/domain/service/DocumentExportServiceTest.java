package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ExportFormat;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.BusinessPlanDocumentRepository;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * DocumentExportService 단위 테스트.
 * 문서 내보내기 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentExportService 단위 테스트")
class DocumentExportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private BusinessPlanDocumentRepository documentRepository;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private DocumentExportService documentExportService;

    private Project testProject;
    private BusinessPlanDocument testDocument;

    @BeforeEach
    void setUp() {
        // Project 엔티티 생성 (Builder 사용)
        testProject = Project.builder()
                .id("test-project-id")
                .templateCode(TemplateCode.KSTARTUP_2025)
                .title("테스트 프로젝트")
                .status(ProjectStatus.SUBMITTED)  // 유효한 상태값 사용
                .userId("default-user")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // BusinessPlanDocument 엔티티 생성 (정적 팩토리 메서드 사용)
        testDocument = BusinessPlanDocument.createNew("test-project-id", 1);
        testDocument.updateAllSections(
                "요약 내용",
                "문제 정의",
                "솔루션 설명",
                "시장 분석",
                "비즈니스 모델",
                "경쟁 분석",
                "마케팅 전략",
                "팀 소개",
                "재무 계획",
                "마일스톤"
        );
        testDocument.markAsGenerated(1000);
    }

    @Nested
    @DisplayName("exportDocument 메서드")
    class ExportDocumentTest {

        @Test
        @DisplayName("HTML 형식으로 문서 내보내기 성공")
        void exportDocument_HtmlFormat_ReturnsHtmlBytes() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(documentRepository.findFirstByProjectIdOrderByVersionDesc("test-project-id"))
                    .willReturn(Optional.of(testDocument));
            given(templateEngine.process(anyString(), any(Context.class)))
                    .willReturn("<html><body>테스트 문서</body></html>");

            // when
            byte[] result = documentExportService.exportDocument("test-project-id", ExportFormat.HTML);

            // then
            assertThat(result).isNotEmpty();
            String htmlContent = new String(result);
            assertThat(htmlContent).contains("테스트 문서");
        }

        @Test
        @DisplayName("null projectId로 내보내기 시 NullPointerException 발생")
        void exportDocument_WithNullProjectId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> documentExportService.exportDocument(null, ExportFormat.PDF))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("프로젝트 ID는 필수입니다");
        }

        @Test
        @DisplayName("null format으로 내보내기 시 NullPointerException 발생")
        void exportDocument_WithNullFormat_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> documentExportService.exportDocument("test-project-id", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("출력 포맷은 필수입니다");
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 ID로 내보내기 시 예외 발생")
        void exportDocument_WithNonExistingProjectId_ThrowsIllegalArgumentException() {
            // given
            given(projectRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> documentExportService.exportDocument("non-existing-id", ExportFormat.PDF))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("생성된 문서가 없는 경우 예외 발생")
        void exportDocument_WithNoDocument_ThrowsIllegalStateException() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(documentRepository.findFirstByProjectIdOrderByVersionDesc("test-project-id"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> documentExportService.exportDocument("test-project-id", ExportFormat.PDF))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("생성된 사업계획서가 없습니다");
        }
    }

    @Nested
    @DisplayName("exportDocumentVersion 메서드")
    class ExportDocumentVersionTest {

        @Test
        @DisplayName("특정 버전의 문서 내보내기 성공")
        void exportDocumentVersion_WithValidVersion_ReturnsDocument() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(documentRepository.findByProjectIdAndVersion("test-project-id", 1))
                    .willReturn(Optional.of(testDocument));
            given(templateEngine.process(anyString(), any(Context.class)))
                    .willReturn("<html><body>버전 1 문서</body></html>");

            // when
            byte[] result = documentExportService.exportDocumentVersion("test-project-id", 1, ExportFormat.HTML);

            // then
            assertThat(result).isNotEmpty();
            String htmlContent = new String(result);
            assertThat(htmlContent).contains("버전 1 문서");
        }

        @Test
        @DisplayName("존재하지 않는 버전 요청 시 예외 발생")
        void exportDocumentVersion_WithNonExistingVersion_ThrowsIllegalArgumentException() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));
            given(documentRepository.findByProjectIdAndVersion("test-project-id", 999))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> documentExportService.exportDocumentVersion("test-project-id", 999, ExportFormat.PDF))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 버전의 문서를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("null projectId로 버전 내보내기 시 NullPointerException 발생")
        void exportDocumentVersion_WithNullProjectId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> documentExportService.exportDocumentVersion(null, 1, ExportFormat.PDF))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("프로젝트 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("generateFileName 메서드")
    class GenerateFileNameTest {

        @Test
        @DisplayName("프로젝트 제목이 있는 경우 제목 기반 파일명 생성")
        void generateFileName_WithTitle_ReturnsFileNameWithTitle() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            String fileName = documentExportService.generateFileName("test-project-id", ExportFormat.PDF);

            // then
            assertThat(fileName).startsWith("테스트_프로젝트_");
            assertThat(fileName).endsWith(".pdf");
        }

        @Test
        @DisplayName("프로젝트 제목이 없는 경우 기본 파일명 생성")
        void generateFileName_WithoutTitle_ReturnsDefaultFileName() {
            // given
            testProject.updateTitle(null);
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            String fileName = documentExportService.generateFileName("test-project-id", ExportFormat.PDF);

            // then
            assertThat(fileName).startsWith("bizplan_");
            assertThat(fileName).endsWith(".pdf");
        }

        @Test
        @DisplayName("프로젝트가 없는 경우 기본 파일명 생성")
        void generateFileName_WithNoProject_ReturnsDefaultFileName() {
            // given
            given(projectRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when
            String fileName = documentExportService.generateFileName("non-existing-id", ExportFormat.HTML);

            // then
            assertThat(fileName).startsWith("bizplan_");
            assertThat(fileName).endsWith(".html");
        }

        @Test
        @DisplayName("HTML 형식 파일명 확장자 검증")
        void generateFileName_HtmlFormat_HasHtmlExtension() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            String fileName = documentExportService.generateFileName("test-project-id", ExportFormat.HTML);

            // then
            assertThat(fileName).endsWith(".html");
        }

        @Test
        @DisplayName("PDF 형식 파일명 확장자 검증")
        void generateFileName_PdfFormat_HasPdfExtension() {
            // given
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            String fileName = documentExportService.generateFileName("test-project-id", ExportFormat.PDF);

            // then
            assertThat(fileName).endsWith(".pdf");
        }

        @Test
        @DisplayName("null projectId로 파일명 생성 시 NullPointerException 발생")
        void generateFileName_WithNullProjectId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> documentExportService.generateFileName(null, ExportFormat.PDF))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("프로젝트 ID는 필수입니다");
        }

        @Test
        @DisplayName("null format으로 파일명 생성 시 NullPointerException 발생")
        void generateFileName_WithNullFormat_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> documentExportService.generateFileName("test-project-id", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("출력 포맷은 필수입니다");
        }

        @Test
        @DisplayName("특수문자가 포함된 제목은 언더스코어로 치환")
        void generateFileName_WithSpecialCharsInTitle_ReplacesWithUnderscore() {
            // given
            testProject.updateTitle("테스트/프로젝트*제목");
            given(projectRepository.findById("test-project-id")).willReturn(Optional.of(testProject));

            // when
            String fileName = documentExportService.generateFileName("test-project-id", ExportFormat.PDF);

            // then
            assertThat(fileName).doesNotContain("/");
            assertThat(fileName).doesNotContain("*");
        }
    }

    @Nested
    @DisplayName("ExportFormat enum 테스트")
    class ExportFormatTest {

        @Test
        @DisplayName("PDF 형식의 ContentType 및 Extension 검증")
        void exportFormat_PDF_CorrectValues() {
            // then
            assertThat(ExportFormat.PDF.getContentType()).isEqualTo("application/pdf");
            assertThat(ExportFormat.PDF.getExtension()).isEqualTo(".pdf");
        }

        @Test
        @DisplayName("HTML 형식의 ContentType 및 Extension 검증")
        void exportFormat_HTML_CorrectValues() {
            // then
            assertThat(ExportFormat.HTML.getContentType()).isEqualTo("text/html");
            assertThat(ExportFormat.HTML.getExtension()).isEqualTo(".html");
        }
    }
}

