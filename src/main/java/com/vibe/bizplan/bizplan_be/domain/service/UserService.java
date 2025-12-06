package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.exception.PasswordMismatchException;
import com.vibe.bizplan.bizplan_be.domain.exception.UserNotFoundException;
import com.vibe.bizplan.bizplan_be.domain.model.UserStatus;
import com.vibe.bizplan.bizplan_be.dto.request.ChangePasswordRequest;
import com.vibe.bizplan.bizplan_be.dto.request.DeleteAccountRequest;
import com.vibe.bizplan.bizplan_be.dto.request.UpdateProfileRequest;
import com.vibe.bizplan.bizplan_be.dto.response.UserProfileResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 서비스.
 * 프로필 조회/수정, 비밀번호 변경, 회원 탈퇴 기능을 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 프로필 조회.
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필 응답
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        log.debug("프로필 조회 - userId={}", userId);
        
        User user = findActiveUser(userId);
        return UserProfileResponse.from(user);
    }

    /**
     * 사용자 프로필 수정.
     *
     * @param userId 사용자 ID
     * @param request 프로필 수정 요청
     * @return 수정된 프로필 응답
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        log.info("프로필 수정 시도 - userId={}", userId);
        
        User user = findActiveUser(userId);
        user.updateProfile(request.name(), request.companyName(), request.phone());
        User savedUser = userRepository.save(user);
        
        log.info("프로필 수정 완료 - userId={}", userId);
        return UserProfileResponse.from(savedUser);
    }

    /**
     * 비밀번호 변경.
     *
     * @param userId 사용자 ID
     * @param request 비밀번호 변경 요청
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws PasswordMismatchException 비밀번호가 일치하지 않는 경우
     */
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        log.info("비밀번호 변경 시도 - userId={}", userId);
        
        User user = findActiveUser(userId);
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: userId={}", userId);
            throw PasswordMismatchException.currentPasswordMismatch();
        }
        
        // 새 비밀번호 확인
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            log.warn("비밀번호 변경 실패 - 새 비밀번호 확인 불일치: userId={}", userId);
            throw PasswordMismatchException.newPasswordConfirmMismatch();
        }
        
        // 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.changePassword(encodedPassword);
        userRepository.save(user);
        
        log.info("비밀번호 변경 완료 - userId={}", userId);
    }

    /**
     * 회원 탈퇴 (소프트 삭제).
     *
     * @param userId 사용자 ID
     * @param request 탈퇴 요청 (비밀번호 확인)
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws PasswordMismatchException 비밀번호가 일치하지 않는 경우
     */
    @Transactional
    public void deleteAccount(String userId, DeleteAccountRequest request) {
        log.info("회원 탈퇴 시도 - userId={}", userId);
        
        User user = findActiveUser(userId);
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("회원 탈퇴 실패 - 비밀번호 불일치: userId={}", userId);
            throw PasswordMismatchException.currentPasswordMismatch();
        }
        
        // 소프트 삭제 처리
        user.changeStatus(UserStatus.DELETED);
        userRepository.save(user);
        
        log.info("회원 탈퇴 완료 - userId={}", userId);
    }

    /**
     * 활성 상태의 사용자 조회.
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws UserNotFoundException 사용자를 찾을 수 없거나 비활성 상태인 경우
     * @throws IllegalArgumentException userId가 null인 경우
     */
    private User findActiveUser(String userId) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(userId, "userId must not be null"))
                .orElseThrow(() -> {
                    log.warn("사용자 조회 실패 - 사용자 없음: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("사용자 조회 실패 - 비활성 상태: userId={}, status={}", userId, user.getStatus());
            throw new UserNotFoundException(userId);
        }
        
        return user;
    }
}

