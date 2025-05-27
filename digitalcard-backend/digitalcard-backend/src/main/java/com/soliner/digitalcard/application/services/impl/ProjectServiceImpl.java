package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.mapper.ProjectMapper;
import com.soliner.digitalcard.application.services.interfaces.ProjectService;
import com.soliner.digitalcard.application.services.interfaces.UserService; // UserService'i import edin
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.domain.model.Project;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.ProjectRepository;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProjectService arayüzünün implementasyonu.
 * Proje iş mantığı operasyonlarını gerçekleştirir ve Repository aracılığıyla veri erişimi sağlar.
 * application katmanına aittir.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserService userService; // UserRepository yerine UserService enjekte edildi

    // Constructor Injection: Tüm bağımlılıklar enjekte edildi
    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper, UserService userService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest projectRequest) {
        // 1. Kullanıcıyı bulma (UserService aracılığıyla)
        User user = userService.getUserById(projectRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", projectRequest.getUserId()));

        // 2. DTO'dan Entity'ye dönüşüm
        Project project = projectMapper.toEntity(projectRequest);
        project.setUser(user); // İlişkili kullanıcıyı set et

        // 3. Entity'yi kaydetme
        Project savedProject = projectRepository.save(project);

        // 4. Kaydedilen Entity'den Response DTO'ya dönüşüm ve döndürme
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest projectRequest) {
        // 1. Mevcut projeyi bulma
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proje", "ID", id));

        // 2. Kullanıcı ID'si değişiyorsa veya geçerli değilse kontrol et ve güncelle
        // Not: Genellikle bir projenin kullanıcısı değişmez.
        // Eğer bu iş kuralı ise, aşağıdaki if bloğunu kaldırabilirsiniz.
        // Eğer değişebilir ve InvalidInputException fırlatmak istiyorsanız, onu da ekleyebilirsiniz.
        if (projectRequest.getUserId() != null &&
            (existingProject.getUser() == null || !existingProject.getUser().getId().equals(projectRequest.getUserId()))) {
            User newUser = userService.getUserById(projectRequest.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", projectRequest.getUserId()));
            existingProject.setUser(newUser);
        }

        // 3. DTO'daki verileri mevcut Entity üzerine güncelleme
        projectMapper.updateEntityFromDto(projectRequest, existingProject);

        // 4. Güncellenen Entity'yi kaydetme
        Project updatedProject = projectRepository.save(existingProject);

        // 5. Güncellenen Entity'den Response DTO'ya dönüşüm ve döndürme
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        // 1. Projeyi bulma
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proje", "ID", id));
        
        // 2. Entity'den Response DTO'ya dönüşüm ve döndürme
        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getProjectsByUserId(Long userId) {
        // 1. Kullanıcının varlığını kontrol etme (iyi bir pratik)
        // Eğer kullanıcı yoksa, ona ait projeler de olamaz.
        userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", userId));

        // 2. Kullanıcıya ait projeleri Repository'den çekme
        List<Project> projects = projectRepository.findByUserId(userId);

        // 3. Entity listesinden Response DTO listesine dönüşüm ve döndürme
        return projects.stream()
                .map(projectMapper::toResponse) // Her bir Project'i ProjectResponse'a dönüştür
                .collect(Collectors.toList()); // Listeye topla
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        // 1. Projenin varlığını kontrol etme
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proje", "ID", id);
        }
        // 2. Projeyi silme
        projectRepository.deleteById(id);
    }
}
