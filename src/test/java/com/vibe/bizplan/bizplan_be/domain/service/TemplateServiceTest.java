package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.dto.response.TemplateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TemplateService 단위 테스트.
 * 외부 의존성이 없는 순수 단위 테스트.
 */
@DisplayName("TemplateService 단위 테스트")
class TemplateServiceTest {

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService();
    }

    @Nested
    @DisplayName("getAllTemplates 메서드")
    class GetAllTemplatesTest {

        @Test
        @DisplayName("모든 템플릿 목록 조회 성공")
        void getAllTemplates_ReturnsAllTemplates() {
            // when
            List<TemplateResponse> result = templateService.getAllTemplates();

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(TemplateCode.values().length);
        }

        @Test
        @DisplayName("템플릿 목록에 필수 필드가 포함됨")
        void getAllTemplates_ContainsRequiredFields() {
            // when
            List<TemplateResponse> result = templateService.getAllTemplates();

            // then
            result.forEach(template -> {
                assertThat(template.code()).isNotBlank();
                assertThat(template.displayName()).isNotBlank();
                assertThat(template.description()).isNotBlank();
                assertThat(template.category()).isNotBlank();
            });
        }
    }

    @Nested
    @DisplayName("getTemplatesByCategory 메서드")
    class GetTemplatesByCategoryTest {

        @Test
        @DisplayName("government 카테고리 템플릿 조회 성공")
        void getTemplatesByCategory_Government_ReturnsGovernmentTemplates() {
            // when
            List<TemplateResponse> result = templateService.getTemplatesByCategory("government");

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(t -> t.category().equalsIgnoreCase("government"));
            assertThat(result).extracting(TemplateResponse::code)
                    .contains("KSTARTUP_2025", "KSTARTUP_EARLY_2025");
        }

        @Test
        @DisplayName("bank 카테고리 템플릿 조회 성공")
        void getTemplatesByCategory_Bank_ReturnsBankTemplates() {
            // when
            List<TemplateResponse> result = templateService.getTemplatesByCategory("bank");

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(t -> t.category().equalsIgnoreCase("bank"));
            assertThat(result).extracting(TemplateResponse::code)
                    .contains("BANK_LOAN_2025");
        }

        @Test
        @DisplayName("investor 카테고리 템플릿 조회 성공")
        void getTemplatesByCategory_Investor_ReturnsInvestorTemplates() {
            // when
            List<TemplateResponse> result = templateService.getTemplatesByCategory("investor");

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(t -> t.category().equalsIgnoreCase("investor"));
            assertThat(result).extracting(TemplateResponse::code)
                    .contains("INVESTOR_PITCH_2025");
        }

        @Test
        @DisplayName("대소문자 구분 없이 카테고리 조회 성공")
        void getTemplatesByCategory_CaseInsensitive_ReturnsTemplates() {
            // when
            List<TemplateResponse> upperResult = templateService.getTemplatesByCategory("GOVERNMENT");
            List<TemplateResponse> lowerResult = templateService.getTemplatesByCategory("government");
            List<TemplateResponse> mixedResult = templateService.getTemplatesByCategory("Government");

            // then
            assertThat(upperResult).hasSameSizeAs(lowerResult);
            assertThat(lowerResult).hasSameSizeAs(mixedResult);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 조회 시 빈 목록 반환")
        void getTemplatesByCategory_NonExisting_ReturnsEmptyList() {
            // when
            List<TemplateResponse> result = templateService.getTemplatesByCategory("non-existing");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplateByCode 메서드")
    class GetTemplateByCodeTest {

        @Test
        @DisplayName("유효한 템플릿 코드로 조회 성공")
        void getTemplateByCode_WithValidCode_ReturnsTemplate() {
            // when
            Optional<TemplateResponse> result = templateService.getTemplateByCode("KSTARTUP_2025");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().code()).isEqualTo("KSTARTUP_2025");
            assertThat(result.get().displayName()).isEqualTo("예비창업패키지 2025");
        }

        @Test
        @DisplayName("소문자 템플릿 코드로 조회 성공")
        void getTemplateByCode_WithLowercaseCode_ReturnsTemplate() {
            // when
            Optional<TemplateResponse> result = templateService.getTemplateByCode("kstartup_2025");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().code()).isEqualTo("KSTARTUP_2025");
        }

        @Test
        @DisplayName("유효하지 않은 템플릿 코드로 조회 시 빈 Optional 반환")
        void getTemplateByCode_WithInvalidCode_ReturnsEmptyOptional() {
            // when
            Optional<TemplateResponse> result = templateService.getTemplateByCode("INVALID_CODE");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("validateTemplateCode 메서드")
    class ValidateTemplateCodeTest {

        @Test
        @DisplayName("유효한 템플릿 코드 검증 성공")
        void validateTemplateCode_WithValidCode_ReturnsTemplateCode() {
            // when
            TemplateCode result = templateService.validateTemplateCode("KSTARTUP_2025");

            // then
            assertThat(result).isEqualTo(TemplateCode.KSTARTUP_2025);
        }

        @Test
        @DisplayName("소문자 템플릿 코드 검증 성공")
        void validateTemplateCode_WithLowercaseCode_ReturnsTemplateCode() {
            // when
            TemplateCode result = templateService.validateTemplateCode("bank_loan_2025");

            // then
            assertThat(result).isEqualTo(TemplateCode.BANK_LOAN_2025);
        }

        @Test
        @DisplayName("유효하지 않은 템플릿 코드 검증 시 예외 발생")
        void validateTemplateCode_WithInvalidCode_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> templateService.validateTemplateCode("INVALID_CODE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 템플릿 코드입니다");
        }

        @Test
        @DisplayName("빈 문자열 템플릿 코드 검증 시 예외 발생")
        void validateTemplateCode_WithEmptyString_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> templateService.validateTemplateCode(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TemplateCode enum 속성 테스트")
    class TemplateCodeEnumTest {

        @Test
        @DisplayName("각 템플릿은 고유한 totalSteps 값을 가짐")
        void templateCode_HasTotalSteps() {
            // then
            assertThat(TemplateCode.KSTARTUP_2025.getTotalSteps()).isEqualTo(12);
            assertThat(TemplateCode.KSTARTUP_EARLY_2025.getTotalSteps()).isEqualTo(10);
            assertThat(TemplateCode.BANK_LOAN_2025.getTotalSteps()).isEqualTo(8);
            assertThat(TemplateCode.INVESTOR_PITCH_2025.getTotalSteps()).isEqualTo(10);
        }

        @Test
        @DisplayName("모든 템플릿은 양수의 totalSteps 값을 가짐")
        void templateCode_TotalStepsIsPositive() {
            // then
            for (TemplateCode code : TemplateCode.values()) {
                assertThat(code.getTotalSteps()).isPositive();
            }
        }
    }
}

