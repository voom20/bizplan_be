package com.vibe.bizplan.bizplan_be.infrastructure.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter: 문자열 필드의 자동 암호화/복호화.
 * Entity 필드에 @Convert(converter = EncryptedStringConverter.class)를 적용하면
 * DB 저장 시 자동으로 암호화되고, 조회 시 자동으로 복호화된다.
 */
@Slf4j
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final AesEncryptionUtil aesEncryptionUtil;

    /**
     * Spring DI를 통해 암호화 유틸리티를 주입받는다.
     * 
     * @param aesEncryptionUtil AES 암호화 유틸리티
     */
    public EncryptedStringConverter(AesEncryptionUtil aesEncryptionUtil) {
        this.aesEncryptionUtil = aesEncryptionUtil;
        log.info("EncryptedStringConverter 초기화 완료");
    }

    /**
     * Entity → DB: 평문을 암호화하여 저장한다.
     * 
     * @param attribute Entity의 평문 값
     * @return DB에 저장할 암호문
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        
        try {
            String encrypted = aesEncryptionUtil.encrypt(attribute);
            log.debug("데이터 암호화 완료: 원본 길이={}, 암호문 길이={}", 
                    attribute.length(), encrypted.length());
            return encrypted;
        } catch (Exception e) {
            log.error("DB 저장 시 암호화 실패", e);
            throw new RuntimeException("데이터 암호화에 실패했습니다", e);
        }
    }

    /**
     * DB → Entity: 암호문을 복호화하여 반환한다.
     * 
     * @param dbData DB에서 읽은 암호문
     * @return Entity에 설정할 평문
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        
        try {
            String decrypted = aesEncryptionUtil.decrypt(dbData);
            log.debug("데이터 복호화 완료: 암호문 길이={}, 원본 길이={}", 
                    dbData.length(), decrypted.length());
            return decrypted;
        } catch (Exception e) {
            log.error("DB 조회 시 복호화 실패", e);
            // 복호화 실패 시 원본 반환 (마이그레이션 중 기존 평문 데이터 처리)
            log.warn("복호화 실패로 원본 데이터 반환 (평문 데이터일 수 있음)");
            return dbData;
        }
    }
}

