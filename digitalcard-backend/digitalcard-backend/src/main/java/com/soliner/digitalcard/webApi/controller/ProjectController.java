package com.soliner.digitalcard.webApi.controller;

import com.soliner.digitalcard.application.services.interfaces.ProjectService; // ProjectService arayüzünü import ediyoruz
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest; // ProjectRequest DTO'yu import ediyoruz
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse; // ProjectResponse DTO'yu import ediyoruz

import jakarta.validation.Valid; // İstek gövdesi validasyonu için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // API yanıtları için
import org.springframework.web.bind.annotation.*; // RESTful anotasyonlar için

import java.util.List;
// Kaldırılan import'lar (Controller katmanında doğrudan kullanılmadıkları için):
// import com.soliner.digitalcard.application.services.interfaces.UserService;
// import com.soliner.digitalcard.application.mapper.ProjectMapper;
// import com.soliner.digitalcard.domain.model.Project;
// import com.soliner.digitalcard.domain.model.User;
// import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
// import java.util.Optional;
// import java.util.stream.Collectors;


/**
 * Projelerle ilgili RESTful API endpoint'lerini yöneten Controller sınıfı.
 * Gelen HTTP isteklerini işler, servis katmanını çağırır ve DTO'lar aracılığıyla yanıt döner.
 * webApi katmanına aittir.
 */
@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/projects") // Tüm endpoint'ler için temel URL yolu (örn: /api/projects)
public class ProjectController {

    private final ProjectService projectService; // Proje iş mantığı için servis katmanı

    // Constructor Injection ile bağımlılıkları enjekte ediyoruz. Sadece ProjectService'e bağımlı.
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Belirli bir kullanıcıya ait tüm projeleri listeler.
     * HTTP Metodu: GET
     * Endpoint: /api/projects/user/{userId}
     * @param userId Projelerin ait olduğu kullanıcının ID'si.
     * @return Projelerin listesini içeren ProjectResponse nesneleri ve 200 OK durumu.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByUserId(@PathVariable Long userId) {
        // Servis katmanı zaten DTO listesi döndürüyor, Controller'da dönüşüme gerek yok.
        List<ProjectResponse> projectResponses = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(projectResponses);
    }

    /**
     * Belirli bir ID'ye sahip projeyi getirir.
     * HTTP Metodu: GET
     * Endpoint: /api/projects/{id}
     * @param id Projenin benzersiz ID'si (URL yolundan alınır).
     * @return Bulunan projeye ait ProjectResponse nesnesi ve 200 OK durumu.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        // Servis katmanı zaten DTO döndürüyor ve ResourceNotFoundException fırlatıyor.
        // GlobalExceptionHandler bu istisnayı 404'e çevirecek.
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * Yeni bir proje oluşturur.
     * HTTP Metodu: POST
     * Endpoint: /api/projects
     * @param projectRequest Oluşturulacak projenin bilgilerini içeren ProjectRequest DTO'su (istek gövdesinden alınır).
     * @return Oluşturulan projeye ait ProjectResponse nesnesi ve 201 Created durumu.
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest projectRequest) {
        // Controller sadece DTO'yu servise iletir. Kullanıcı kontrolü ve DTO-Entity dönüşümü servisin sorumluluğundadır.
        ProjectResponse createdProject = projectService.createProject(projectRequest);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    /**
     * Mevcut bir projeyi günceller.
     * HTTP Metodu: PUT
     * Endpoint: /api/projects/{id}
     * @param id Güncellenecek projenin benzersiz ID'si.
     * @param projectRequest Güncelleme bilgilerini içeren ProjectRequest DTO'su.
     * @return Güncellenen projeye ait ProjectResponse nesnesi ve 200 OK durumu.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest projectRequest) {
        // Controller sadece DTO'yu servise iletir. Güncelleme mantığı ve DTO-Entity dönüşümü servisin sorumluluğundadır.
        ProjectResponse updatedProject = projectService.updateProject(id, projectRequest);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Belirli bir ID'ye sahip projeyi siler.
     * HTTP Metodu: DELETE
     * Endpoint: /api/projects/{id}
     * @param id Silinecek projenin benzersiz ID'si.
     * @return 204 No Content durumu.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content döner
    public void deleteProject(@PathVariable Long id) {
        // Servis katmanı varlık kontrolünü ve silmeyi yapar. ResourceNotFoundException fırlatırsa
        // GlobalExceptionHandler 404'e çevirir.
        projectService.deleteProject(id);
    }
}
