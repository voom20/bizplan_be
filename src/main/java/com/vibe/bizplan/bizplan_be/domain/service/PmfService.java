package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.dto.request.PmfSubmitRequest;
import com.vibe.bizplan.bizplan_be.dto.response.PmfQuestionResponse;
import com.vibe.bizplan.bizplan_be.dto.response.PmfQuestionResponse.QuestionOption;
import com.vibe.bizplan.bizplan_be.dto.response.PmfQuestionsResponse;
import com.vibe.bizplan.bizplan_be.dto.response.PmfReportResponse;
import com.vibe.bizplan.bizplan_be.dto.response.PmfReportResponse.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PMF(Product-Market Fit) 서비스.
 * PMF 설문 질문 제공 및 결과 분석 기능을 담당한다.
 * MVP에서는 인메모리 저장소를 사용한다.
 */
@Slf4j
@Service
public class PmfService {

    // MVP: 인메모리 저장소 (프로젝트ID -> PMF 리포트)
    private final Map<String, PmfReportResponse> reportStore = new ConcurrentHashMap<>();

    /**
     * PMF 설문 질문 목록 조회.
     * MVP: 하드코딩된 Sean Ellis 테스트 기반 질문.
     *
     * @return PMF 질문 목록
     */
    public PmfQuestionsResponse getQuestions() {
        log.info("[PMF] 설문 질문 조회");
        
        List<PmfQuestionResponse> questions = List.of(
                new PmfQuestionResponse(
                        "pmf-1",
                        "만약 이 제품을 더 이상 사용할 수 없게 된다면 어떻게 느끼시겠습니까?",
                        "radio",
                        true,
                        List.of(
                                new QuestionOption(1, "매우 실망할 것이다"),
                                new QuestionOption(2, "다소 실망할 것이다"),
                                new QuestionOption(3, "실망하지 않을 것이다"),
                                new QuestionOption(4, "해당 없음 (이미 사용하지 않음)")
                        )
                ),
                new PmfQuestionResponse(
                        "pmf-2",
                        "이 제품의 주요 혜택은 무엇입니까?",
                        "multiselect",
                        true,
                        List.of(
                                new QuestionOption(1, "시간 절약"),
                                new QuestionOption(2, "비용 절감"),
                                new QuestionOption(3, "품질/성능 향상"),
                                new QuestionOption(4, "편의성 증가"),
                                new QuestionOption(5, "기타")
                        )
                ),
                new PmfQuestionResponse(
                        "pmf-3",
                        "이 제품을 가장 잘 설명하는 대상은 누구입니까?",
                        "radio",
                        true,
                        List.of(
                                new QuestionOption(1, "창업자/스타트업"),
                                new QuestionOption(2, "중소기업 경영자"),
                                new QuestionOption(3, "대기업 직원"),
                                new QuestionOption(4, "프리랜서"),
                                new QuestionOption(5, "기타")
                        )
                ),
                new PmfQuestionResponse(
                        "pmf-4",
                        "이 제품을 어떻게 알게 되셨습니까?",
                        "radio",
                        false,
                        List.of(
                                new QuestionOption(1, "지인 추천"),
                                new QuestionOption(2, "검색 엔진"),
                                new QuestionOption(3, "소셜 미디어"),
                                new QuestionOption(4, "광고"),
                                new QuestionOption(5, "기타")
                        )
                ),
                new PmfQuestionResponse(
                        "pmf-5",
                        "이 제품을 다른 사람에게 추천할 의향이 있으십니까? (0-10점)",
                        "radio",
                        true,
                        List.of(
                                new QuestionOption(0, "0 - 전혀 추천하지 않음"),
                                new QuestionOption(1, "1"),
                                new QuestionOption(2, "2"),
                                new QuestionOption(3, "3"),
                                new QuestionOption(4, "4"),
                                new QuestionOption(5, "5"),
                                new QuestionOption(6, "6"),
                                new QuestionOption(7, "7"),
                                new QuestionOption(8, "8"),
                                new QuestionOption(9, "9"),
                                new QuestionOption(10, "10 - 적극 추천")
                        )
                )
        );
        
        return new PmfQuestionsResponse(questions);
    }

    /**
     * PMF 설문 결과 제출 및 분석.
     *
     * @param projectId 프로젝트 ID
     * @param request 설문 답변
     * @return PMF 분석 리포트
     */
    public PmfReportResponse submitAndAnalyze(String projectId, PmfSubmitRequest request) {
        log.info("[PMF] 설문 결과 제출 - projectId={}, answers={}", projectId, request.answers().size());
        
        // PMF 점수 계산
        int score = calculatePmfScore(request);
        String level = determinePmfLevel(score);
        
        // 리스크 및 추천사항 생성
        List<Risk> risks = generateRisks(score, request);
        List<Recommendation> recommendations = generateRecommendations(score, request);
        
        // 답변 변환
        List<PmfAnswer> answers = request.answers().stream()
                .map(a -> new PmfAnswer(a.questionId(), a.value()))
                .toList();
        
        PmfReportResponse report = new PmfReportResponse(
                projectId,
                score,
                level,
                risks,
                recommendations,
                answers,
                LocalDateTime.now()
        );
        
        // 저장
        reportStore.put(projectId, report);
        
        log.info("[PMF] 분석 완료 - projectId={}, score={}, level={}", projectId, score, level);
        return report;
    }

    /**
     * 저장된 PMF 리포트 조회.
     *
     * @param projectId 프로젝트 ID
     * @return PMF 리포트 (Optional)
     */
    public Optional<PmfReportResponse> getReport(String projectId) {
        log.info("[PMF] 리포트 조회 - projectId={}", projectId);
        return Optional.ofNullable(reportStore.get(projectId));
    }

    /**
     * PMF 점수 계산.
     * Sean Ellis 테스트: "매우 실망할 것이다" 비율이 40% 이상이면 PMF 달성.
     */
    private int calculatePmfScore(PmfSubmitRequest request) {
        // pmf-1 질문의 답변 확인 (핵심 질문)
        Optional<Object> pmf1Answer = request.answers().stream()
                .filter(a -> "pmf-1".equals(a.questionId()))
                .map(PmfSubmitRequest.PmfAnswerRequest::value)
                .findFirst();
        
        int baseScore = 25; // 기본 점수
        
        if (pmf1Answer.isPresent()) {
            Object value = pmf1Answer.get();
            int answerValue = value instanceof Number ? ((Number) value).intValue() : 3;
            
            // 1 = 매우 실망 (높은 점수), 4 = 해당 없음 (낮은 점수)
            baseScore = switch (answerValue) {
                case 1 -> 45; // 매우 실망 -> 높은 PMF
                case 2 -> 30; // 다소 실망 -> 중간 PMF
                case 3 -> 15; // 실망하지 않음 -> 낮은 PMF
                default -> 10; // 해당 없음
            };
        }
        
        // NPS 질문 (pmf-5) 보너스 점수
        Optional<Object> npsAnswer = request.answers().stream()
                .filter(a -> "pmf-5".equals(a.questionId()))
                .map(PmfSubmitRequest.PmfAnswerRequest::value)
                .findFirst();
        
        if (npsAnswer.isPresent()) {
            Object value = npsAnswer.get();
            int nps = value instanceof Number ? ((Number) value).intValue() : 5;
            if (nps >= 9) baseScore += 10; // Promoter 보너스
            else if (nps >= 7) baseScore += 5; // Passive 보너스
        }
        
        return Math.min(100, baseScore);
    }

    /**
     * PMF 레벨 결정.
     */
    private String determinePmfLevel(int score) {
        if (score >= 40) return "excellent";
        if (score >= 30) return "high";
        if (score >= 20) return "medium";
        return "low";
    }

    /**
     * 리스크 생성.
     */
    private List<Risk> generateRisks(int score, PmfSubmitRequest request) {
        List<Risk> risks = new ArrayList<>();
        
        if (score < 40) {
            risks.add(new Risk(
                    "risk-1",
                    "PMF 미달성",
                    "현재 제품이 시장의 핵심 니즈를 충족하지 못하고 있습니다.",
                    score < 20 ? "high" : "medium"
            ));
        }
        
        if (score < 30) {
            risks.add(new Risk(
                    "risk-2",
                    "시장 검증 필요",
                    "더 많은 고객 피드백과 시장 검증이 필요합니다.",
                    "medium"
            ));
        }
        
        return risks;
    }

    /**
     * 추천사항 생성.
     */
    private List<Recommendation> generateRecommendations(int score, PmfSubmitRequest request) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        if (score < 40) {
            recommendations.add(new Recommendation(
                    "rec-1",
                    "고객 인터뷰 진행",
                    "핵심 고객층과 심층 인터뷰를 통해 제품 개선점을 파악하세요.",
                    "high"
            ));
        }
        
        if (score < 30) {
            recommendations.add(new Recommendation(
                    "rec-2",
                    "MVP 피벗 고려",
                    "현재 접근 방식이 효과적이지 않을 수 있습니다. 피벗을 고려해보세요.",
                    "high"
            ));
        }
        
        if (score >= 40) {
            recommendations.add(new Recommendation(
                    "rec-3",
                    "스케일업 준비",
                    "PMF가 달성되었습니다. 마케팅 확대와 성장 전략을 수립하세요.",
                    "medium"
            ));
        }
        
        return recommendations;
    }
}

