package com.soliner.digitalcard.webApi.controller;

import com.soliner.digitalcard.application.services.interfaces.ProjectService;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse;
// KRİTİK DÜZELTME: UserDetailsImpl import yolu güncellendi
import com.soliner.digitalcard.application.services.impl.UserDetailsImpl; 
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Projelerle ilgili RESTful API endpoint'lerini yöneten Controller sınıfı.
 * Gelen HTTP isteklerini işler, servis katmanını çağırır ve DTO'lar aracılığıyla yanıt döner.
 * webApi katmanına aittir.
 */
@RestController
@RequestMapping("/api/users/{userId}/projects")
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable Long userId,
            @Valid @RequestBody ProjectRequest projectRequest) {
        log.info("Kullanıcı ID: {} için proje oluşturma isteği alındı.", userId);
        validateUserOwnership(userId); 
        ProjectResponse newProject = projectService.createProject(userId, projectRequest);
        log.info("Kullanıcı ID: {} için proje başarıyla oluşturuldu: {}", userId, newProject.getTitle());
        return new ResponseEntity<>(newProject, HttpStatus.CREATED);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {
        log.info("Proje ID: {} için getirme isteği alındı.", projectId);
        ProjectResponse project = projectService.getProjectById(projectId);
        log.info("Proje ID: {} başarıyla getirildi: {}", projectId, project.getTitle());
        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjectsByUserId(@PathVariable Long userId) {
        log.info("Kullanıcı ID: {} için tüm projeleri getirme isteği alındı.", userId);
        validateUserOwnership(userId);
        List<ProjectResponse> projects = projectService.getAllProjectsByUserId(userId);
        log.info("Kullanıcı ID: {} için {} proje bulundu.", userId, projects.size());
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long userId,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest projectRequest) {
        log.info("Kullanıcı ID: {} ve Proje ID: {} için güncelleme isteği alındı.", userId, projectId);
        validateUserOwnership(userId);
        ProjectResponse updatedProject = projectService.updateProject(userId, projectId, projectRequest);
        log.info("Kullanıcı ID: {} ve Proje ID: {} başarıyla güncellendi: {}", userId, projectId, updatedProject.getTitle());
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @PathVariable Long userId,
            @PathVariable Long projectId) {
        log.info("Kullanıcı ID: {} ve Proje ID: {} için silme isteği alındı.", userId, projectId);
        validateUserOwnership(userId);
        projectService.deleteProject(userId, projectId);
        log.info("Kullanıcı ID: {} ve Proje ID: {} başarıyla silindi.", userId, projectId);
    }

    private void validateUserOwnership(Long pathUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Kimlik doğrulaması yapılmamış veya anonim kullanıcıdan yetkisiz erişim denemesi.");
            throw new AccessDeniedException("Kimlik doğrulaması yapılmamış kullanıcı.");
        }

        Object principal = authentication.getPrincipal();
        Long authenticatedUserId;

        // KRİTİK DÜZELTME: UserDetailsImpl sınıfınızın doğru paketi kullanıldı.
        if (principal instanceof UserDetailsImpl) {
            authenticatedUserId = ((UserDetailsImpl) principal).getId();
        } else if (principal instanceof UserDetails) {
            log.warn("Principal tipi UserDetailsImpl değil, standart UserDetails. Kullanıcı ID'sine doğrudan erişilemiyor. Username: {}", ((UserDetails) principal).getUsername());
            throw new AccessDeniedException("Kullanıcı kimliği doğrulaması yapılamadı: ID'ye erişim hatası.");
        } else {
            log.warn("Bilinmeyen principal tipi: {}", principal.getClass().getName());
            throw new AccessDeniedException("Erişim reddedildi: Kullanıcı doğrulanamadı.");
        }
        
        if (!authenticatedUserId.equals(pathUserId)) {
            log.warn("Erişim reddedildi: Kullanıcı ID {} (kimliği doğrulanmış) ile path ID {} eşleşmiyor.", authenticatedUserId, pathUserId);
            throw new AccessDeniedException("Bu kaynağa erişim yetkiniz yok.");
        }
        log.debug("Kullanıcı ID: {} için sahip kontrolü başarılı.", pathUserId);
    }
}
