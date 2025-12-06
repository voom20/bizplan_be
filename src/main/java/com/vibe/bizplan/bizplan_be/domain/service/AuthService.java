package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.exception.DuplicateEmailException;
import com.vibe.bizplan.bizplan_be.domain.exception.InvalidCredentialsException;
import com.vibe.bizplan.bizplan_be.domain.exception.InvalidTokenException;
import com.vibe.bizplan.bizplan_be.domain.model.UserStatus;
import com.vibe.bizplan.bizplan_be.dto.request.LoginRequest;
import com.vibe.bizplan.bizplan_be.dto.request.RefreshTokenRequest;
import com.vibe.bizplan.bizplan_be.dto.request.SignupRequest;
import com.vibe.bizplan.bizplan_be.dto.response.SignupResponse;
import com.vibe.bizplan.bizplan_be.dto.response.TokenResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.UserRepository;
import com.vibe.bizplan.bizplan_be.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 서비스.
 * 회원가입, 로그인, 로그아웃, 토큰 갱신 기능을 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입 처리.
     *
     * @param request 회원가입 요청
     * @return 생성된 사용자 정보
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("회원가입 시도 - email={}", request.email());

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            log.warn("회원가입 실패 - 이메일 중복: {}", request.email());
            throw new DuplicateEmailException(request.email());
        }

        // 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(request.password());

        // 사용자 생성
        User user = User.create(request.email(), encodedPassword, request.name());
        User savedUser = userRepository.save(user);

        log.info("회원가입 성공 - userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return SignupResponse.from(savedUser);
    }

    /**
     * 로그인 처리.
     *
     * @param request 로그인 요청
     * @return JWT 토큰
     * @throws InvalidCredentialsException 인증 실패 시
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("로그인 시도 - email={}", request.email());

        // 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자 없음: {}", request.email());
                    return new InvalidCredentialsException();
                });

        // 계정 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("로그인 실패 - 비활성 계정: {}, status={}", request.email(), user.getStatus());
            throw new InvalidCredentialsException("계정이 비활성 상태입니다");
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("로그인 실패 - 비밀번호 불일치: {}", request.email());
            throw new InvalidCredentialsException();
        }

        // 마지막 로그인 시간 업데이트
        user.updateLastLoginAt();
        userRepository.save(user);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("로그인 성공 - userId={}, email={}", user.getId(), user.getEmail());

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    /**
     * 토큰 갱신 처리.
     *
     * @param request 토큰 갱신 요청
     * @return 새로운 JWT 토큰
     * @throws InvalidTokenException 리프레시 토큰이 유효하지 않은 경우
     */
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        log.debug("토큰 갱신 시도");

        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            log.warn("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰");
            throw new InvalidTokenException("리프레시 토큰이 만료되었거나 유효하지 않습니다");
        }

        // 토큰에서 사용자 정보 추출
        String email = jwtTokenProvider.getUsernameFromToken(request.refreshToken());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("토큰 갱신 실패 - 사용자 없음: {}", email);
                    return new InvalidTokenException("사용자를 찾을 수 없습니다");
                });

        // 계정 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("토큰 갱신 실패 - 비활성 계정: {}", email);
            throw new InvalidTokenException("계정이 비활성 상태입니다");
        }

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("토큰 갱신 성공 - email={}", email);

        return TokenResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    /**
     * 로그아웃 처리.
     * 현재 JWT 기반 stateless 인증이므로 클라이언트에서 토큰 삭제로 처리.
     * 향후 토큰 블랙리스트 기능 추가 가능.
     *
     * @param userId 사용자 ID
     */
    public void logout(String userId) {
        log.info("로그아웃 처리 - userId={}", userId);
        // TODO: 토큰 블랙리스트 추가 (Redis 활용 시)
    }
}

