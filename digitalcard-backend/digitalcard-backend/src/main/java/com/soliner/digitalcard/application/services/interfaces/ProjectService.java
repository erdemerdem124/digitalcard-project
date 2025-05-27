	package com.soliner.digitalcard.application.services.interfaces;
	
	import com.soliner.digitalcard.webApi.dto.project.ProjectRequest; // BURASI ARTIK KULLANILACAK
	import com.soliner.digitalcard.webApi.dto.project.ProjectResponse; // BURASI ARTIK KULLANILACAK
	
	import java.util.List;
	// import com.soliner.digitalcard.domain.model.Project; // Artık doğrudan kullanılmıyor, servis DTO döndürüyor
	// import com.soliner.digitalcard.domain.model.User; // Artık doğrudan kullanılmıyor, servis DTO döndürüyor
	// import java.util.Optional; // getProjectById artık Optional döndürmüyor
	
	/**
	 * Proje iş mantığı operasyonları için arayüz.
	 * Bu arayüz, webApi katmanındaki Controller'lar tarafından çağrılır.
	 * application katmanına aittir.
	 */
	public interface ProjectService {
	
	    // 1. Yeni bir proje oluşturma metodu (ProjectRequest alıp ProjectResponse döndürüyor)
	    ProjectResponse createProject(ProjectRequest projectRequest);
	
	    // 2. Mevcut bir projeyi güncelleme metodu (ProjectRequest alıp ProjectResponse döndürüyor)
	    ProjectResponse updateProject(Long id, ProjectRequest projectRequest);
	
	    // 3. Belirli bir ID'ye sahip projeyi getirme metodu (ProjectResponse döndürüyor)
	    // Eğer proje bulunamazsa ResourceNotFoundException fırlatılacak, Controller'da Optional'a gerek kalmayacak.
	    ProjectResponse getProjectById(Long id);
	
	    // 4. Belirli bir kullanıcıya ait tüm projeleri getirme metodu (List<ProjectResponse> döndürüyor)
	    List<ProjectResponse> getProjectsByUserId(Long userId); // Metot adı ve parametre tipi güncellendi
	
	    // 5. Belirli bir ID'ye sahip projeyi silme metodu
	    void deleteProject(Long id);
	
	    // NOT: getAllProjects() ve getProjectsByUser(User user) gibi metotlar Controller'da artık DTO döndüren versiyonlarıyla kullanılmalı.
	    // Eğer getAllProjects() metoduna ihtiyacınız varsa, List<ProjectResponse> getAllProjects(); şeklinde ekleyebilirsiniz.
	}
