package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.exception.PasswordMismatchException;
import com.vibe.bizplan.bizplan_be.domain.exception.UserNotFoundException;
import com.vibe.bizplan.bizplan_be.domain.model.UserRole;
import com.vibe.bizplan.bizplan_be.domain.model.UserStatus;
import com.vibe.bizplan.bizplan_be.dto.request.ChangePasswordRequest;
import com.vibe.bizplan.bizplan_be.dto.request.DeleteAccountRequest;
import com.vibe.bizplan.bizplan_be.dto.request.UpdateProfileRequest;
import com.vibe.bizplan.bizplan_be.dto.response.UserProfileResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * UserService 단위 테스트.
 * 사용자 프로필 관리, 비밀번호 변경, 회원 탈퇴 기능을 테스트한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
@SuppressWarnings("null")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String CURRENT_PASSWORD = "currentPassword123";
    private static final String HASHED_PASSWORD = "hashedPassword";

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .passwordHash(HASHED_PASSWORD)
                .name("테스트 사용자")
                .companyName("테스트 회사")
                .phone("010-1234-5678")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getProfile 메서드")
    class GetProfileTest {

        @Test
        @DisplayName("활성 사용자 프로필 조회 성공")
        void getProfile_WithActiveUser_ReturnsProfile() {
            // given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

            // when
            UserProfileResponse result = userService.getProfile(TEST_USER_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(TEST_USER_ID);
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
            assertThat(result.name()).isEqualTo("테스트 사용자");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void getProfile_WithNonExistingUser_ThrowsUserNotFoundException() {
            // given
            given(userRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getProfile("non-existing-id"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("비활성 사용자 조회 시 예외 발생")
        void getProfile_WithInactiveUser_ThrowsUserNotFoundException() {
            // given
            User suspendedUser = User.builder()
                    .id(TEST_USER_ID)
                    .email(TEST_EMAIL)
                    .passwordHash(HASHED_PASSWORD)
                    .role(UserRole.USER)
                    .status(UserStatus.SUSPENDED)
                    .build();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(suspendedUser));

            // when & then
            assertThatThrownBy(() -> userService.getProfile(TEST_USER_ID))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("삭제된 사용자 조회 시 예외 발생")
        void getProfile_WithDeletedUser_ThrowsUserNotFoundException() {
            // given
            User deletedUser = User.builder()
                    .id(TEST_USER_ID)
                    .email(TEST_EMAIL)
                    .passwordHash(HASHED_PASSWORD)
                    .role(UserRole.USER)
                    .status(UserStatus.DELETED)
                    .build();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(deletedUser));

            // when & then
            assertThatThrownBy(() -> userService.getProfile(TEST_USER_ID))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("null userId로 조회 시 NullPointerException 발생")
        void getProfile_WithNullUserId_ThrowsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> userService.getProfile(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("updateProfile 메서드")
    class UpdateProfileTest {

        @Test
        @DisplayName("프로필 수정 성공")
        void updateProfile_WithValidRequest_ReturnsUpdatedProfile() {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("새이름", "새회사", "010-9999-9999");
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            UserProfileResponse result = userService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("이름만 수정 시 성공")
        void updateProfile_WithOnlyName_ReturnsUpdatedProfile() {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("새이름", null, null);
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            UserProfileResponse result = userService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 프로필 수정 시 예외 발생")
        void updateProfile_WithNonExistingUser_ThrowsUserNotFoundException() {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("새이름", null, null);
            given(userRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateProfile("non-existing-id", request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("비활성 사용자 프로필 수정 시 예외 발생")
        void updateProfile_WithInactiveUser_ThrowsUserNotFoundException() {
            // given
            User suspendedUser = User.builder()
                    .id(TEST_USER_ID)
                    .email(TEST_EMAIL)
                    .passwordHash(HASHED_PASSWORD)
                    .role(UserRole.USER)
                    .status(UserStatus.SUSPENDED)
                    .build();
            UpdateProfileRequest request = new UpdateProfileRequest("새이름", null, null);
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(suspendedUser));

            // when & then
            assertThatThrownBy(() -> userService.updateProfile(TEST_USER_ID, request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("changePassword 메서드")
    class ChangePasswordTest {

        @Test
        @DisplayName("비밀번호 변경 성공")
        void changePassword_WithValidRequest_Success() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                    CURRENT_PASSWORD, "newPassword123", "newPassword123");
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_PASSWORD)).willReturn(true);
            given(passwordEncoder.encode("newPassword123")).willReturn("newHashedPassword");
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            userService.changePassword(TEST_USER_ID, request);

            // then
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("newPassword123");
        }

        @Test
        @DisplayName("현재 비밀번호 불일치 시 예외 발생")
        void changePassword_WithWrongCurrentPassword_ThrowsPasswordMismatchException() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "wrongPassword", "newPassword123", "newPassword123");
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", HASHED_PASSWORD)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.changePassword(TEST_USER_ID, request))
                    .isInstanceOf(PasswordMismatchException.class)
                    .hasMessageContaining("현재 비밀번호가 일치하지 않습니다");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("새 비밀번호 확인 불일치 시 예외 발생")
        void changePassword_WithMismatchedNewPassword_ThrowsPasswordMismatchException() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                    CURRENT_PASSWORD, "newPassword123", "differentPassword");
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_PASSWORD)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.changePassword(TEST_USER_ID, request))
                    .isInstanceOf(PasswordMismatchException.class)
                    .hasMessageContaining("새 비밀번호가 일치하지 않습니다");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 비밀번호 변경 시 예외 발생")
        void changePassword_WithNonExistingUser_ThrowsUserNotFoundException() {
            // given
            ChangePasswordRequest request = new ChangePasswordRequest(
                    CURRENT_PASSWORD, "newPassword123", "newPassword123");
            given(userRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.changePassword("non-existing-id", request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteAccount 메서드")
    class DeleteAccountTest {

        @Test
        @DisplayName("회원 탈퇴 성공")
        void deleteAccount_WithValidRequest_Success() {
            // given
            DeleteAccountRequest request = new DeleteAccountRequest(CURRENT_PASSWORD);
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_PASSWORD)).willReturn(true);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            userService.deleteAccount(TEST_USER_ID, request);

            // then
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("비밀번호 불일치 시 탈퇴 실패")
        void deleteAccount_WithWrongPassword_ThrowsPasswordMismatchException() {
            // given
            DeleteAccountRequest request = new DeleteAccountRequest("wrongPassword");
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", HASHED_PASSWORD)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.deleteAccount(TEST_USER_ID, request))
                    .isInstanceOf(PasswordMismatchException.class)
                    .hasMessageContaining("현재 비밀번호가 일치하지 않습니다");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 탈퇴 시 예외 발생")
        void deleteAccount_WithNonExistingUser_ThrowsUserNotFoundException() {
            // given
            DeleteAccountRequest request = new DeleteAccountRequest(CURRENT_PASSWORD);
            given(userRepository.findById("non-existing-id")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deleteAccount("non-existing-id", request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("이미 삭제된 사용자 탈퇴 시 예외 발생")
        void deleteAccount_WithDeletedUser_ThrowsUserNotFoundException() {
            // given
            User deletedUser = User.builder()
                    .id(TEST_USER_ID)
                    .email(TEST_EMAIL)
                    .passwordHash(HASHED_PASSWORD)
                    .role(UserRole.USER)
                    .status(UserStatus.DELETED)
                    .build();
            DeleteAccountRequest request = new DeleteAccountRequest(CURRENT_PASSWORD);
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(deletedUser));

            // when & then
            assertThatThrownBy(() -> userService.deleteAccount(TEST_USER_ID, request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("프로필 응답 검증")
    class ProfileResponseValidationTest {

        @Test
        @DisplayName("프로필 응답에 모든 필드가 포함됨")
        void getProfile_ResponseContainsAllFields() {
            // given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(testUser));

            // when
            UserProfileResponse result = userService.getProfile(TEST_USER_ID);

            // then
            assertThat(result.id()).isEqualTo(TEST_USER_ID);
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
            assertThat(result.name()).isEqualTo("테스트 사용자");
            assertThat(result.companyName()).isEqualTo("테스트 회사");
            assertThat(result.phone()).isEqualTo("010-1234-5678");
            assertThat(result.role()).isEqualTo("일반 사용자");
            assertThat(result.lastLoginAt()).isNotNull();
            assertThat(result.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("PREMIUM 사용자 역할이 올바르게 표시됨")
        void getProfile_PremiumUserRoleDisplayedCorrectly() {
            // given
            User premiumUser = User.builder()
                    .id("premium-user-id")
                    .email("premium@example.com")
                    .passwordHash(HASHED_PASSWORD)
                    .role(UserRole.PREMIUM)
                    .status(UserStatus.ACTIVE)
                    .build();
            given(userRepository.findById("premium-user-id")).willReturn(Optional.of(premiumUser));

            // when
            UserProfileResponse result = userService.getProfile("premium-user-id");

            // then
            assertThat(result.role()).isEqualTo("프리미엄 사용자");
        }

        @Test
        @DisplayName("ADMIN 사용자 역할이 올바르게 표시됨")
        void getProfile_AdminUserRoleDisplayedCorrectly() {
            // given
            User adminUser = User.builder()
                    .id("admin-user-id")
                    .email("admin@example.com")
                    .passwordHash(HASHED_PASSWORD)
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            given(userRepository.findById("admin-user-id")).willReturn(Optional.of(adminUser));

            // when
            UserProfileResponse result = userService.getProfile("admin-user-id");

            // then
            assertThat(result.role()).isEqualTo("관리자");
        }
    }
}

