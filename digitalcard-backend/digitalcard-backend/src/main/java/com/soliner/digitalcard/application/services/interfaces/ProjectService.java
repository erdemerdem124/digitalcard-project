package com.soliner.digitalcard.application.services.interfaces;

import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse;

import java.util.List;

/**
 * Proje iş mantığı operasyonları için arayüz.
 * Bu arayüz, webApi katmanındaki Controller'lar tarafından çağrılır.
 * application katmanına aittir.
 */
public interface ProjectService {

    // Yeni bir proje oluşturma metodu (userId ve ProjectRequest alıp ProjectResponse döndürüyor)
    ProjectResponse createProject(Long userId, ProjectRequest projectRequest);

    // Mevcut bir projeyi güncelleme metodu (userId, id ve ProjectRequest alıp ProjectResponse döndürüyor)
    ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest projectRequest);

    // Belirli bir ID'ye sahip projeyi getirme metodu (ProjectResponse döndürüyor)
    // Eğer proje bulunamazsa ResourceNotFoundException fırlatılacak, Controller'da Optional'a gerek kalmayacak.
    ProjectResponse getProjectById(Long projectId);

    // Belirli bir kullanıcıya ait tüm projeleri getirme metodu (List<ProjectResponse> döndürüyor)
    List<ProjectResponse> getAllProjectsByUserId(Long userId);

    // Belirli bir ID'ye sahip projeyi, belirli bir kullanıcının silme metodu
    void deleteProject(Long userId, Long projectId);

    // KRİTİK EKLENTİ: Tüm projeleri getirme metodu eklendi
    List<ProjectResponse> getAllProjects();
}
