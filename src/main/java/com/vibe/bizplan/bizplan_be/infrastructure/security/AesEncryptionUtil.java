package com.vibe.bizplan.bizplan_be.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 암호화 유틸리티.
 * 민감한 데이터의 암호화/복호화를 담당한다.
 * GCM 모드는 기밀성과 무결성을 동시에 보장한다.
 */
@Slf4j
@Component
public class AesEncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  // 96 bits (권장)
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    private final SecretKeySpec secretKey;

    /**
     * 생성자: 환경변수에서 암호화 키를 로드한다.
     * 키는 Base64 인코딩된 32바이트(256비트) 문자열이어야 한다.
     * 
     * @param encryptionKey Base64 인코딩된 AES-256 키
     */
    public AesEncryptionUtil(
            @Value("${app.security.encryption-key:#{null}}") String encryptionKey
    ) {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            // MVP: 기본 키 사용 (운영 환경에서는 반드시 환경변수로 설정해야 함)
            log.warn("암호화 키가 설정되지 않았습니다. 기본 키를 사용합니다. 운영 환경에서는 반드시 app.security.encryption-key를 설정하세요!");
            encryptionKey = "Yml6cGxhbl9lbmNyeXB0aW9uX2tleV8zMmJ5dGVzISE="; // 32 bytes base64
        }
        
        byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("암호화 키는 32바이트(256비트)여야 합니다. 현재: " + keyBytes.length + "바이트");
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        log.info("AES-256-GCM 암호화 유틸리티 초기화 완료");
    }

    /**
     * 문자열을 AES-256-GCM으로 암호화한다.
     * IV는 암호문 앞에 붙여서 반환한다 (IV + 암호문).
     * 
     * @param plainText 암호화할 평문
     * @return Base64 인코딩된 암호문 (IV 포함)
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // 랜덤 IV 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            
            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            // 암호화
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // IV + 암호문 결합
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (Exception e) {
            log.error("암호화 실패", e);
            throw new RuntimeException("데이터 암호화에 실패했습니다", e);
        }
    }

    /**
     * AES-256-GCM으로 암호화된 문자열을 복호화한다.
     * 
     * @param encryptedText Base64 인코딩된 암호문 (IV 포함)
     * @return 복호화된 평문
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            // Base64 디코딩
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            // IV 추출
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            
            // 암호문 추출
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            
            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            // 복호화
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("복호화 실패", e);
            throw new RuntimeException("데이터 복호화에 실패했습니다", e);
        }
    }
}

