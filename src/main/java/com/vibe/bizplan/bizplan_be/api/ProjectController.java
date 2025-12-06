package com.vibe.bizplan.bizplan_be.api;

import com.vibe.bizplan.bizplan_be.domain.service.ProjectService;
import com.vibe.bizplan.bizplan_be.domain.service.TemplateService;
import com.vibe.bizplan.bizplan_be.dto.request.CreateProjectRequest;
import com.vibe.bizplan.bizplan_be.dto.response.ProjectResponse;
import com.vibe.bizplan.bizplan_be.dto.response.TemplateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 프로젝트 REST API 컨트롤러.
 * 프로젝트 생성, 조회 및 템플릿 목록 조회 API를 제공한다.
 */
@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "사업계획서 프로젝트 관리 API")
public class ProjectController {

    private final ProjectService projectService;
    private final TemplateService templateService;

    /**
     * 지원 템플릿 목록 조회.
     * 하드코딩된 템플릿 목록(예비창업패키지, 은행대출용 등)을 반환한다.
     *
     * @return 템플릿 목록
     */
    @GetMapping("/templates")
    @Operation(summary = "템플릿 목록 조회", description = "사용 가능한 사업계획서 템플릿 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TemplateResponse.class)))
    })
    public ResponseEntity<List<TemplateResponse>> getTemplates(
            @Parameter(description = "템플릿 카테고리 필터 (government, bank, investor)")
            @RequestParam(required = false) String category
    ) {
        List<TemplateResponse> templates;
        if (category != null && !category.isBlank()) {
            templates = templateService.getTemplatesByCategory(category);
        } else {
            templates = templateService.getAllTemplates();
        }
        log.debug("[Project] 템플릿 조회 완료 - category={}, count={}", category, templates.size());
        return ResponseEntity.ok(templates);
    }

    /**
     * 새 프로젝트 생성.
     * 선택된 템플릿으로 새 사업계획서 프로젝트를 생성한다.
     *
     * @param request 프로젝트 생성 요청
     * @return 생성된 프로젝트 정보 (HTTP 201)
     */
    @PostMapping
    @Operation(summary = "프로젝트 생성", description = "선택된 템플릿으로 새 사업계획서 프로젝트를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 템플릿 코드 등)")
    })
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        ProjectResponse response = projectService.createProject(request);
        log.debug("[Project] 프로젝트 생성 완료 - projectId={}, templateCode={}", 
                response.projectId(), request.templateCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 프로젝트 상세 조회.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 정보
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 조회", description = "프로젝트 ID로 프로젝트 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    public ResponseEntity<ProjectResponse> getProject(
            @Parameter(description = "프로젝트 ID") @PathVariable String projectId
    ) {
        return projectService.getProject(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 내 프로젝트 목록 조회.
     * MVP에서는 기본 사용자의 프로젝트만 반환.
     *
     * @return 프로젝트 목록
     */
    @GetMapping
    @Operation(summary = "내 프로젝트 목록", description = "현재 사용자의 프로젝트 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    })
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        List<ProjectResponse> projects = projectService.getMyProjects();
        log.debug("[Project] 프로젝트 목록 조회 완료 - count={}", projects.size());
        return ResponseEntity.ok(projects);
    }
}

