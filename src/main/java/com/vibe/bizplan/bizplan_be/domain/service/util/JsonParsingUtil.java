package com.vibe.bizplan.bizplan_be.domain.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON 파싱 유틸리티 클래스.
 * JSON 문자열과 Map 간의 변환을 담당한다.
 * WizardService, BusinessPlanOrchestrationService 등에서 공통으로 사용.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonParsingUtil {

    private final ObjectMapper objectMapper;

    /**
     * JSON 문자열을 Map으로 파싱.
     * 
     * @param json JSON 문자열 (null 허용)
     * @return 파싱된 Map, 실패 또는 null/빈 문자열인 경우 빈 Map 반환
     */
    public Map<String, Object> parseToMap(String json) {
        if (json == null || json.isBlank()) {
            log.debug("[JsonParsing] JSON 파싱 스킵 - 데이터 없음 (null 또는 빈 문자열)");
            return new HashMap<>();
        }
        try {
            Map<String, Object> result = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            log.debug("[JsonParsing] JSON 파싱 완료 - entriesCount={}", result.size());
            return result;
        } catch (JsonProcessingException e) {
            log.error("[JsonParsing] JSON 파싱 실패 - jsonLength={}, error={}", json.length(), e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * JSON 문자열을 Map으로 파싱 (실패 시 빈 불변 Map 반환).
     * 읽기 전용 컨텍스트에서 사용.
     * 
     * @param json JSON 문자열 (null 허용)
     * @return 파싱된 Map (불변), 실패 시 빈 불변 Map 반환
     */
    public Map<String, Object> parseToImmutableMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("[JsonParsing] JSON 파싱 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Map을 JSON 문자열로 변환.
     * 
     * @param data 변환할 Map 데이터
     * @return JSON 문자열
     * @throws RuntimeException JSON 변환 실패 시
     */
    public String toJson(Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            log.debug("[JsonParsing] JSON 변환 완료 - entriesCount={}, jsonLength={}", data.size(), json.length());
            return json;
        } catch (JsonProcessingException e) {
            log.error("[JsonParsing] JSON 변환 실패 - entriesCount={}, error={}", data.size(), e.getMessage(), e);
            throw new RuntimeException("JSON 데이터 변환에 실패했습니다", e);
        }
    }
}

