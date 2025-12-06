package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.dto.response.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 템플릿 서비스.
 * 사용 가능한 사업계획서 템플릿 목록을 제공한다.
 * MVP에서는 하드코딩된 목록을 반환한다.
 */
@Slf4j
@Service
public class TemplateService {

    /**
     * 모든 템플릿 목록 조회.
     *
     * @return 템플릿 응답 DTO 목록
     */
    public List<TemplateResponse> getAllTemplates() {
        log.debug("전체 템플릿 목록 조회");
        return Arrays.stream(TemplateCode.values())
                .map(TemplateResponse::from)
                .toList();
    }

    /**
     * 특정 카테고리의 템플릿 목록 조회.
     *
     * @param category 카테고리 (government, bank, investor)
     * @return 해당 카테고리의 템플릿 목록
     */
    public List<TemplateResponse> getTemplatesByCategory(String category) {
        log.debug("카테고리별 템플릿 목록 조회: {}", category);
        return Arrays.stream(TemplateCode.values())
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .map(TemplateResponse::from)
                .toList();
    }

    /**
     * 템플릿 코드로 템플릿 조회.
     *
     * @param code 템플릿 코드 문자열
     * @return 템플릿 응답 DTO (Optional)
     */
    public Optional<TemplateResponse> getTemplateByCode(String code) {
        log.debug("템플릿 코드로 조회: {}", code);
        try {
            TemplateCode templateCode = TemplateCode.valueOf(code.toUpperCase());
            return Optional.of(TemplateResponse.from(templateCode));
        } catch (IllegalArgumentException e) {
            log.warn("존재하지 않는 템플릿 코드: {}", code);
            return Optional.empty();
        }
    }

    /**
     * 템플릿 코드 유효성 검증.
     *
     * @param code 템플릿 코드 문자열
     * @return 유효한 코드인 경우 TemplateCode, 아니면 예외 발생
     * @throws IllegalArgumentException 유효하지 않은 템플릿 코드인 경우
     */
    public TemplateCode validateTemplateCode(String code) {
        try {
            return TemplateCode.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("유효하지 않은 템플릿 코드: {}", code);
            throw new IllegalArgumentException("유효하지 않은 템플릿 코드입니다: " + code);
        }
    }
}

