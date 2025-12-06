package com.vibe.bizplan.bizplan_be.domain.entity;

import com.vibe.bizplan.bizplan_be.domain.model.UserRole;
import com.vibe.bizplan.bizplan_be.domain.model.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 사용자 엔티티.
 * Spring Security의 UserDetails를 구현하여 인증/인가에 사용된다.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User implements UserDetails {

    /** 사용자 고유 ID (UUID) */
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    /** 로그인 이메일 (유니크) */
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    /** BCrypt 해시된 비밀번호 */
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    /** 사용자 이름 */
    @Column(name = "name", length = 100)
    private String name;

    /** 회사/프로젝트명 */
    @Column(name = "company_name", length = 200)
    private String companyName;

    /** 연락처 (선택) */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 사용자 역할 */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /** 계정 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /** 마지막 로그인 시간 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 생성일시 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 소프트 삭제 시간 */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ============================================
    // 정적 팩토리 메서드
    // ============================================

    /**
     * 신규 사용자 생성을 위한 정적 팩토리 메서드.
     *
     * @param email 이메일
     * @param passwordHash BCrypt 해시된 비밀번호
     * @param name 이름 (선택)
     * @return 생성된 User 인스턴스 (non-null 보장)
     */
    @NonNull
    public static User create(String email, String passwordHash, String name) {
        return Objects.requireNonNull(User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(passwordHash)
                .name(name)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());
    }

    // ============================================
    // 도메인 메서드
    // ============================================

    /**
     * 마지막 로그인 시간 업데이트.
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 프로필 정보 업데이트.
     *
     * @param name 이름
     * @param companyName 회사명
     * @param phone 연락처
     */
    public void updateProfile(String name, String companyName, String phone) {
        if (name != null) {
            this.name = name;
        }
        if (companyName != null) {
            this.companyName = companyName;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }

    /**
     * 비밀번호 변경.
     *
     * @param newPasswordHash 새로운 BCrypt 해시된 비밀번호
     */
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /**
     * 계정 상태 변경.
     *
     * @param status 새로운 상태
     */
    public void changeStatus(UserStatus status) {
        this.status = status;
        if (status == UserStatus.DELETED) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    /**
     * 역할 변경.
     *
     * @param role 새로운 역할
     */
    public void changeRole(UserRole role) {
        this.role = role;
    }

    // ============================================
    // UserDetails 인터페이스 구현
    // ============================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return status != UserStatus.DELETED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}

