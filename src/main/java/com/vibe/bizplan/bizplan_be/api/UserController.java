package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.service.UserService;
import com.vibe.bizplan.bizplan_be.dto.request.ChangePasswordRequest;
import com.vibe.bizplan.bizplan_be.dto.request.DeleteAccountRequest;
import com.vibe.bizplan.bizplan_be.dto.request.UpdateProfileRequest;
import com.vibe.bizplan.bizplan_be.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 사용자 관리 API 컨트롤러.
 * 프로필 조회/수정, 비밀번호 변경, 회원 탈퇴 엔드포인트를 제공한다.
 */
@Tag(name = "Users", description = "사용자 관리 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 조회.
     *
     * @param user 현재 인증된 사용자
     * @return 사용자 프로필
     */
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        UserProfileResponse response = userService.getProfile(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 수정.
     *
     * @param user 현재 인증된 사용자
     * @param request 프로필 수정 요청
     * @return 수정된 프로필
     */
    @Operation(summary = "프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경.
     *
     * @param user 현재 인증된 사용자
     * @param request 비밀번호 변경 요청
     * @return 성공 메시지
     */
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치 또는 잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다"));
    }

    /**
     * 회원 탈퇴.
     *
     * @param user 현재 인증된 사용자
     * @param request 탈퇴 요청 (비밀번호 확인)
     * @return 성공 메시지
     */
    @Operation(summary = "회원 탈퇴", description = "비밀번호를 확인하고 계정을 삭제합니다 (소프트 삭제)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DeleteAccountRequest request) {
        userService.deleteAccount(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다"));
    }
}

