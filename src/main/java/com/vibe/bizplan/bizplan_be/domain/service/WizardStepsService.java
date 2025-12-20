package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.dto.response.WizardQuestionResponse;
import com.vibe.bizplan.bizplan_be.dto.response.WizardQuestionResponse.QuestionOption;
import com.vibe.bizplan.bizplan_be.dto.response.WizardStepDefinitionResponse;
import com.vibe.bizplan.bizplan_be.dto.response.WizardStepsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 위저드 단계 정의 서비스.
 * 템플릿별 위저드 단계 및 질문 정의를 제공한다.
 * MVP에서는 하드코딩된 정의를 반환한다.
 */
@Slf4j
@Service
public class WizardStepsService {

    /**
     * 템플릿별 위저드 단계 정의 조회.
     *
     * @param templateCode 템플릿 코드
     * @return 위저드 단계 정의 응답
     */
    public WizardStepsResponse getWizardSteps(TemplateCode templateCode) {
        log.info("[WizardSteps] 위저드 단계 정의 조회 - templateCode={}", templateCode);
        
        // MVP: 모든 템플릿에 동일한 기본 단계 정의 사용
        List<WizardStepDefinitionResponse> steps = getDefaultSteps();
        
        return new WizardStepsResponse(
                templateCode.name().toLowerCase(),
                steps.size(),
                steps
        );
    }

    /**
     * 기본 위저드 단계 정의.
     * MVP용 하드코딩된 단계 정의를 반환한다.
     */
    private List<WizardStepDefinitionResponse> getDefaultSteps() {
        return List.of(
                // Step 1: 사업 아이디어
                new WizardStepDefinitionResponse(
                        1,
                        "사업 아이디어",
                        "사업 아이디어를 설명해주세요",
                        List.of(
                                new WizardQuestionResponse(
                                        "business-name",
                                        "text",
                                        "사업명",
                                        "사업명을 입력하세요",
                                        true,
                                        100,
                                        null
                                ),
                                new WizardQuestionResponse(
                                        "business-description",
                                        "textarea",
                                        "사업 설명",
                                        "사업 아이디어를 설명해주세요",
                                        true,
                                        1000,
                                        null
                                ),
                                new WizardQuestionResponse(
                                        "problem-statement",
                                        "textarea",
                                        "해결하고자 하는 문제",
                                        "고객이 겪고 있는 문제를 설명해주세요",
                                        true,
                                        1000,
                                        null
                                )
                        )
                ),
                // Step 2: 목표 시장
                new WizardStepDefinitionResponse(
                        2,
                        "목표 시장",
                        "목표 시장을 정의해주세요",
                        List.of(
                                new WizardQuestionResponse(
                                        "target-customer",
                                        "textarea",
                                        "목표 고객",
                                        "주요 목표 고객층을 설명해주세요",
                                        true,
                                        500,
                                        null
                                ),
                                new WizardQuestionResponse(
                                        "market-size",
                                        "select",
                                        "시장 규모",
                                        "예상 시장 규모를 선택하세요",
                                        true,
                                        null,
                                        List.of(
                                                new QuestionOption("small", "소규모 (10억 미만)"),
                                                new QuestionOption("medium", "중규모 (10억 ~ 100억)"),
                                                new QuestionOption("large", "대규모 (100억 ~ 1000억)"),
                                                new QuestionOption("enterprise", "엔터프라이즈 (1000억 이상)")
                                        )
                                ),
                                new WizardQuestionResponse(
                                        "competitors",
                                        "textarea",
                                        "경쟁사",
                                        "주요 경쟁사와 차별점을 설명해주세요",
                                        false,
                                        500,
                                        null
                                )
                        )
                ),
                // Step 3: 비즈니스 모델
                new WizardStepDefinitionResponse(
                        3,
                        "비즈니스 모델",
                        "수익 창출 방법을 설명해주세요",
                        List.of(
                                new WizardQuestionResponse(
                                        "revenue-model",
                                        "multiselect",
                                        "수익 모델",
                                        "수익 창출 방법을 선택하세요 (복수 선택 가능)",
                                        true,
                                        null,
                                        List.of(
                                                new QuestionOption("subscription", "구독형"),
                                                new QuestionOption("one-time", "일회성 판매"),
                                                new QuestionOption("commission", "수수료"),
                                                new QuestionOption("advertising", "광고"),
                                                new QuestionOption("freemium", "프리미엄"),
                                                new QuestionOption("other", "기타")
                                        )
                                ),
                                new WizardQuestionResponse(
                                        "pricing",
                                        "textarea",
                                        "가격 정책",
                                        "예상 가격대와 가격 책정 근거를 설명해주세요",
                                        true,
                                        500,
                                        null
                                )
                        )
                ),
                // Step 4: 실행 계획
                new WizardStepDefinitionResponse(
                        4,
                        "실행 계획",
                        "사업 실행 계획을 수립해주세요",
                        List.of(
                                new WizardQuestionResponse(
                                        "milestones",
                                        "textarea",
                                        "주요 마일스톤",
                                        "향후 12개월 주요 달성 목표를 설명해주세요",
                                        true,
                                        1000,
                                        null
                                ),
                                new WizardQuestionResponse(
                                        "team",
                                        "textarea",
                                        "팀 구성",
                                        "현재 팀 구성과 필요한 인력을 설명해주세요",
                                        false,
                                        500,
                                        null
                                ),
                                new WizardQuestionResponse(
                                        "funding-needed",
                                        "number",
                                        "필요 자금 (원)",
                                        "초기 필요 자금을 입력하세요",
                                        true,
                                        null,
                                        null
                                )
                        )
                ),
                // Step 5: 리스크 분석
                new WizardStepDefinitionResponse(
                        5,
                        "리스크 분석",
                        "예상되는 리스크와 대응 방안을 검토합니다",
                        List.of(
                                new WizardQuestionResponse(
                                        "risks",
                                        "textarea",
                                        "주요 리스크",
                                        "예상되는 주요 리스크를 설명해주세요",
                                        true,
                                        1000,
                                        null
                                ),
                                new WizardQuestionResponse(
                                        "mitigation",
                                        "textarea",
                                        "리스크 대응 방안",
                                        "각 리스크에 대한 대응 방안을 설명해주세요",
                                        true,
                                        1000,
                                        null
                                )
                        )
                )
        );
    }
}

