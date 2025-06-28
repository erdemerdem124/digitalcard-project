package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.mapper.ProjectMapper;
import com.soliner.digitalcard.application.services.interfaces.ProjectService;
import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.domain.model.Project;
import com.soliner.digitalcard.domain.model.User; // User entity import edildi
import com.soliner.digitalcard.persistence.repository.ProjectRepository;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional; // Optional import edildi
import java.util.stream.Collectors; // Collectors import edildi

/**
 * Proje yönetimi iş mantığını uygulayan servis sınıfı.
 * application katmanına aittir.
 */
@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserService userService; // UserService'i interface olarak kullanıyoruz

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper, UserService userService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(Long userId, ProjectRequest projectRequest) {
        log.info("createProject metodu çağrıldı. Kullanıcı ID: {}", userId);
        
        // KRİTİK DÜZELTME: Doğrudan User entity'sini getiriyoruz
        User user = userService.getUserEntityById(userId)
                               .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        // Proje başlığının benzersizliğini kontrol et (aynı kullanıcı için)
        if (projectRepository.findByUserAndTitle(user, projectRequest.getTitle()).isPresent()) {
            log.warn("Kullanıcının zaten bu başlıkta bir projesi var: {} - Kullanıcı ID: {}", projectRequest.getTitle(), userId);
            throw new InvalidInputException("Project with title '" + projectRequest.getTitle() + "' already exists for this user.");
        }

        // KRİTİK DÜZELTME: projectMapper.toEntity metoduna 'user' objesi de gönderildi
        Project project = projectMapper.toEntity(projectRequest, user);
        
        Project savedProject = projectRepository.save(project);
        log.info("Proje başarıyla kaydedildi: {} - Kullanıcı ID: {}", savedProject.getTitle(), userId);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest projectRequest) {
        log.info("updateProject metodu çağrıldı. Kullanıcı ID: {}, Proje ID: {}", userId, projectId);
        
        // KRİTİK DÜZELTME: Doğrudan User entity'sini getiriyoruz
        User user = userService.getUserEntityById(userId)
                               .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Güncellenecek proje bulunamadı. Proje ID: {}", projectId);
                    return new ResourceNotFoundException("Project", "ID", projectId);
                });

        // Projenin doğru kullanıcıya ait olduğunu doğrula
        if (!existingProject.getUser().getId().equals(userId)) {
            log.warn("Proje, kullanıcıya ait değil. Proje ID: {}, Kullanıcı ID: {}", projectId, userId);
            throw new InvalidInputException("You are not authorized to update this project or the project does not belong to you.");
        }

        // Başlık değiştiyse ve yeni başlık başka bir projede mevcutsa kontrol et
        if (!existingProject.getTitle().equals(projectRequest.getTitle())) {
            // KRİTİK DÜZELTME: Başlık kontrolü, aynı kullanıcıya ait diğer projeleri içermeli
            if (projectRepository.findByUserAndTitle(user, projectRequest.getTitle()).isPresent()) {
                 log.warn("Kullanıcının zaten bu başlıkta başka bir projesi var: {} - Kullanıcı ID: {}", projectRequest.getTitle(), userId);
                 throw new InvalidInputException("Project with title '" + projectRequest.getTitle() + "' already exists for this user.");
            }
        }

        projectMapper.updateEntityFromDto(projectRequest, existingProject);
        Project updatedProject = projectRepository.save(existingProject);
        log.info("Proje başarıyla güncellendi: {} - Kullanıcı ID: {}", updatedProject.getTitle(), userId);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public ProjectResponse getProjectById(Long projectId) {
        log.info("getProjectById metodu çağrıldı. Proje ID: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Proje bulunamadı. Proje ID: {}", projectId);
                    return new ResourceNotFoundException("Project", "ID", projectId);
                });
        log.info("Proje bulundu: {}", project.getTitle());
        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjectsByUserId(Long userId) {
        log.info("getAllProjectsByUserId metodu çağrıldı. Kullanıcı ID: {}", userId);
        // Kullanıcının varlığını kontrol et
        userService.getUserEntityById(userId)
                   .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
                   
        List<Project> projects = projectRepository.findByUserId(userId);
        List<ProjectResponse> projectResponses = projectMapper.toResponseList(projects);
        log.info("Kullanıcı ID {} için {} proje bulundu.", userId, projectResponses.size());
        return projectResponses;
    }

    @Override
    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        log.info("deleteProject metodu çağrıldı. Kullanıcı ID: {}, Proje ID: {}", userId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Silinecek proje bulunamadı. Proje ID: {}", projectId);
                    return new ResourceNotFoundException("Project", "ID", projectId);
                });

        if (!project.getUser().getId().equals(userId)) {
            log.warn("Proje, kullanıcıya ait değil. Proje ID: {}, Kullanıcı ID: {}", projectId, userId);
            throw new InvalidInputException("You are not authorized to delete this project or the project does not belong to you.");
        }
        
        projectRepository.delete(project);
        log.info("Proje başarıyla silindi: {} - Kullanıcı ID: {}", project.getTitle(), userId);
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        log.info("getAllProjects metodu çağrıldı.");
        List<Project> projects = projectRepository.findAll();
        log.info("{} proje bulundu.", projects.size());
        return projectMapper.toResponseList(projects);
    }
}
