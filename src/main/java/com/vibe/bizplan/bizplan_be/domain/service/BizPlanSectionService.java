package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.BizPlanSection;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.exception.ProjectNotFoundException;
import com.vibe.bizplan.bizplan_be.domain.model.SectionType;
import com.vibe.bizplan.bizplan_be.domain.service.util.JsonParsingUtil;
import com.vibe.bizplan.bizplan_be.dto.request.GenerateSectionRequest;
import com.vibe.bizplan.bizplan_be.dto.request.UpdateSectionRequest;
import com.vibe.bizplan.bizplan_be.dto.response.BizPlanSectionResponse;
import com.vibe.bizplan.bizplan_be.dto.response.BizPlanSectionsListResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.client.AiEngineClient;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.BizPlanGenerateRequest;
import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.BizPlanGenerateResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.BizPlanSectionRepository;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 사업계획서 섹션 서비스.
 * 개별 섹션의 AI 생성, CRUD, 조회를 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizPlanSectionService {

    private final ProjectRepository projectRepository;
    private final BizPlanSectionRepository sectionRepository;
    private final AiEngineClient aiEngineClient;
    private final JsonParsingUtil jsonParsingUtil;

    /**
     * AI를 통해 특정 섹션을 생성한다.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입 코드
     * @param request     생성 요청 DTO
     * @return 생성된 섹션 응답
     */
    @Transactional
    public BizPlanSectionResponse generateSection(String projectId, String sectionType, GenerateSectionRequest request) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(sectionType, "섹션 타입은 필수입니다");
        long startTime = System.currentTimeMillis();
        log.info("[BizPlanSection] 섹션 생성 시작 - projectId={}, sectionType={}, mode={}",
                projectId, sectionType, request != null ? request.getMode() : "easy");

        // 섹션 타입 유효성 검증
        SectionType section = SectionType.fromCode(sectionType)
                .orElseThrow(() -> {
                    log.error("[BizPlanSection] 유효하지 않은 섹션 타입 - sectionType={}", sectionType);
                    return new IllegalArgumentException("유효하지 않은 섹션 타입입니다: " + sectionType);
                });

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.error("[BizPlanSection] 프로젝트 조회 실패 - projectId={}", projectId);
                    return new ProjectNotFoundException(projectId);
                });

        String templateCode = project.getTemplateCode().name();

        // Wizard 답변 파싱
        Map<String, Object> context = jsonParsingUtil.parseToImmutableMap(project.getWizardAnswers());
        log.debug("[BizPlanSection] 컨텍스트 준비 완료 - contextKeys={}", context.keySet());

        // 이전 섹션 내용 조회 (일관성 유지용)
        Map<String, String> previousSections = getPreviousSectionsContent(projectId, section);

        // AI Engine 요청 생성
        String mode = request != null && request.mode() != null ? request.mode() : "easy";
        String additionalInstructions = request != null ? request.additionalInstructions() : null;

        BizPlanGenerateRequest aiRequest = new BizPlanGenerateRequest(
                projectId,
                templateCode,
                sectionType,
                mode,
                context,
                previousSections,
                additionalInstructions
        );

        // AI Engine 호출
        log.debug("[BizPlanSection] AI 엔진 호출 중 - projectId={}, sectionType={}", projectId, sectionType);
        BizPlanGenerateResponse aiResponse = aiEngineClient.generateSection(aiRequest);

        // 결과 저장
        BizPlanSection savedSection = saveOrUpdateSection(projectId, sectionType, aiResponse);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[BizPlanSection] 섹션 생성 완료 - projectId={}, sectionType={}, wordCount={}, duration={}ms",
                projectId, sectionType,
                savedSection != null ? savedSection.getWordCount() : 0, duration);

        // 응답 생성
        return BizPlanSectionResponse.fromAiResult(
                savedSection,
                aiResponse != null ? aiResponse.suggestions() : null,
                aiResponse != null ? aiResponse.consistencyWarnings() : null
        );
    }

    /**
     * 프로젝트의 모든 섹션 목록 조회.
     *
     * @param projectId 프로젝트 ID
     * @return 섹션 목록 응답
     */
    @Transactional(readOnly = true)
    public BizPlanSectionsListResponse getSections(String projectId) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        log.debug("[BizPlanSection] 섹션 목록 조회 - projectId={}", projectId);

        // 프로젝트 존재 여부 확인
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        List<BizPlanSection> sections = sectionRepository.findByProjectIdOrderBySectionType(projectId);
        List<BizPlanSectionsListResponse.SectionSummary> summaries = sections.stream()
                .map(s -> new BizPlanSectionsListResponse.SectionSummary(
                        s.getSectionType(),
                        s.getTitle(),
                        s.getWordCount(),
                        s.getUpdatedAt()
                ))
                .toList();

        log.debug("[BizPlanSection] 섹션 목록 조회 완료 - projectId={}, count={}", projectId, summaries.size());

        return BizPlanSectionsListResponse.of(projectId, summaries);
    }

    /**
     * 특정 섹션 조회.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     * @return 섹션 응답 (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<BizPlanSectionResponse> getSection(String projectId, String sectionType) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(sectionType, "섹션 타입은 필수입니다");
        log.debug("[BizPlanSection] 섹션 조회 - projectId={}, sectionType={}", projectId, sectionType);

        return sectionRepository.findByProjectIdAndSectionType(projectId, sectionType)
                .map(section -> {
                    log.debug("[BizPlanSection] 섹션 조회 완료 - projectId={}, sectionType={}", projectId, sectionType);
                    return BizPlanSectionResponse.from(section);
                });
    }

    /**
     * 섹션 내용 수동 업데이트.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     * @param request     업데이트 요청
     * @return 업데이트된 섹션 응답
     */
    @Transactional
    public BizPlanSectionResponse updateSection(String projectId, String sectionType, UpdateSectionRequest request) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(sectionType, "섹션 타입은 필수입니다");
        Objects.requireNonNull(request, "요청 데이터는 필수입니다");
        log.info("[BizPlanSection] 섹션 수동 업데이트 - projectId={}, sectionType={}", projectId, sectionType);

        // 프로젝트 존재 확인
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        // 기존 섹션 조회 또는 새로 생성
        BizPlanSection section = sectionRepository.findByProjectIdAndSectionType(projectId, sectionType)
                .orElseGet(() -> {
                    log.debug("[BizPlanSection] 새 섹션 생성 - projectId={}, sectionType={}", projectId, sectionType);
                    return BizPlanSection.create(
                            projectId,
                            sectionType,
                            request.title() != null ? request.title() : sectionType,
                            request.content(),
                            request.content() != null ? request.content().length() : 0,
                            null,
                            null
                    );
                });

        // 내용 업데이트
        String title = request.title() != null ? request.title() : section.getTitle();
        section.updateContent(title, request.content());

        BizPlanSection savedSection = sectionRepository.save(section);
        log.info("[BizPlanSection] 섹션 업데이트 완료 - projectId={}, sectionType={}", projectId, sectionType);

        return BizPlanSectionResponse.from(savedSection);
    }

    /**
     * 섹션 삭제.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     */
    @Transactional
    public void deleteSection(String projectId, String sectionType) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(sectionType, "섹션 타입은 필수입니다");
        log.info("[BizPlanSection] 섹션 삭제 - projectId={}, sectionType={}", projectId, sectionType);

        sectionRepository.deleteByProjectIdAndSectionType(projectId, sectionType);
        log.info("[BizPlanSection] 섹션 삭제 완료 - projectId={}, sectionType={}", projectId, sectionType);
    }

    // ==========================================
    // Private Helper Methods
    // ==========================================

    /**
     * AI 응답으로 섹션 저장 또는 업데이트.
     */
    private BizPlanSection saveOrUpdateSection(String projectId, String sectionType, BizPlanGenerateResponse response) {
        if (response == null || response.section() == null) {
            log.warn("[BizPlanSection] AI 응답 없음 - projectId={}, sectionType={}", projectId, sectionType);
            return null;
        }

        BizPlanGenerateResponse.BizPlanSection aiSection = response.section();

        // 기존 섹션 조회
        Optional<BizPlanSection> existingOpt = sectionRepository.findByProjectIdAndSectionType(projectId, sectionType);

        BizPlanSection section;
        if (existingOpt.isPresent()) {
            section = existingOpt.get();
            section.updateFromAi(
                    aiSection.title(),
                    aiSection.content(),
                    aiSection.wordCount(),
                    aiSection.modelUsed(),
                    aiSection.generationTimeMs()
            );
            log.debug("[BizPlanSection] 기존 섹션 업데이트 - projectId={}, sectionType={}", projectId, sectionType);
        } else {
            section = BizPlanSection.create(
                    projectId,
                    sectionType,
                    aiSection.title(),
                    aiSection.content(),
                    aiSection.wordCount(),
                    aiSection.modelUsed(),
                    aiSection.generationTimeMs()
            );
            log.debug("[BizPlanSection] 새 섹션 생성 - projectId={}, sectionType={}", projectId, sectionType);
        }

        return sectionRepository.save(section);
    }

    /**
     * 현재 섹션 이전의 모든 섹션 내용 조회.
     */
    private Map<String, String> getPreviousSectionsContent(String projectId, SectionType currentSection) {
        Map<String, String> previous = new LinkedHashMap<>();

        List<SectionType> orderedSections = SectionType.getOrderedSections();
        for (SectionType section : orderedSections) {
            if (section == currentSection) break;

            sectionRepository.findByProjectIdAndSectionType(projectId, section.getCode())
                    .ifPresent(s -> {
                        if (s.getContent() != null && !s.getContent().isBlank()) {
                            previous.put(section.getCode(), s.getContent());
                        }
                    });
        }

        return previous;
    }
}

