package com.vibe.bizplan.bizplan_be.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vibe.bizplan.bizplan_be.domain.entity.User;
import com.vibe.bizplan.bizplan_be.domain.model.UserRole;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로젝트 리소스에 대한 보안 검사를 수행하는 컴포넌트.
 * @PreAuthorize 어노테이션에서 SpEL을 통해 호출된다.
 */
@Slf4j
@Component("projectSecurity")
@RequiredArgsConstructor
public class ProjectSecurityChecker {

    private final ProjectRepository projectRepository;

    /**
     * 현재 사용자가 해당 프로젝트의 소유자인지 확인.
     * ADMIN 권한을 가진 사용자는 모든 프로젝트에 접근 가능.
     *
     * @param projectId 프로젝트 ID
     * @return 소유자 또는 ADMIN인 경우 true
     */
    public boolean isOwner(String projectId) {
        // Null 체크: projectId가 null이면 접근 거부
        if (projectId == null) {
            log.debug("프로젝트 접근 거부 - projectId가 null입니다");
            return false;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("프로젝트 접근 거부 - 인증되지 않은 요청: projectId={}", projectId);
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            log.debug("프로젝트 접근 거부 - 잘못된 Principal 타입: projectId={}", projectId);
            return false;
        }
        
        User currentUser = (User) principal;
        
        // ADMIN은 모든 프로젝트 접근 가능
        if (currentUser.getRole() == UserRole.ADMIN) {
            log.debug("프로젝트 접근 허용 - ADMIN 권한: userId={}, projectId={}", 
                    currentUser.getId(), projectId);
            return true;
        }
        
        // 프로젝트 소유자 확인
        return projectRepository.findById(projectId)
                .map(project -> {
                    boolean isOwner = project.getUserId().equals(currentUser.getId());
                    if (isOwner) {
                        log.debug("프로젝트 접근 허용 - 소유자: userId={}, projectId={}", 
                                currentUser.getId(), projectId);
                    } else {
                        log.warn("프로젝트 접근 거부 - 소유자 불일치: userId={}, projectId={}, ownerId={}", 
                                currentUser.getId(), projectId, project.getUserId());
                    }
                    return isOwner;
                })
                .orElseGet(() -> {
                    log.debug("프로젝트 접근 - 프로젝트 없음: projectId={}", projectId);
                    return true; // 프로젝트가 없으면 통과 (404 처리는 서비스에서)
                });
    }

    /**
     * 현재 사용자가 프로젝트를 생성할 수 있는지 확인.
     * 무료 사용자는 최대 5개까지만 프로젝트 생성 가능.
     *
     * @return 프로젝트 생성 가능 여부
     */
    public boolean canCreateProject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }
        
        User currentUser = (User) principal;
        
        // ADMIN/PREMIUM은 제한 없음
        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.PREMIUM) {
            return true;
        }
        
        // 일반 사용자는 5개 제한
        long projectCount = projectRepository.countByUserId(currentUser.getId());
        boolean canCreate = projectCount < 5;
        
        if (!canCreate) {
            log.warn("프로젝트 생성 제한 - 최대 개수 초과: userId={}, count={}", 
                    currentUser.getId(), projectCount);
        }
        
        return canCreate;
    }

    /**
     * 현재 인증된 사용자 ID 반환.
     *
     * @return 사용자 ID (인증되지 않은 경우 null)
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        
        return null;
    }
}

