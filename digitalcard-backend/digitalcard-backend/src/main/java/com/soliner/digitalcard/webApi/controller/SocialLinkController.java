package com.soliner.digitalcard.webApi.controller;

import com.soliner.digitalcard.application.services.interfaces.SocialLinkService;
import com.soliner.digitalcard.application.services.impl.UserDetailsImpl; // UserDetailsImpl import edildi
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;
import lombok.extern.slf4j.Slf4j; // Slf4j için eklendi
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Spring Security AccessDeniedException için
import org.springframework.security.core.Authentication; // Authentication import edildi
import org.springframework.security.core.context.SecurityContextHolder; // SecurityContextHolder import edildi
import org.springframework.security.core.userdetails.UserDetails; // UserDetails import edildi
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Sosyal medya linkleri ile ilgili RESTful API endpoint'leri.
 * Gelen HTTP isteklerini karşılar, servis katmanını çağırır ve yanıtları DTO'lar aracılığıyla döndürür.
 * webApi katmanına aittir.
 */
@RestController
@RequestMapping("/api/users/{userId}/social-links")
@Slf4j // Loglama için Lombok anotasyonu
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    /**
     * Yeni bir sosyal medya linki oluşturur.
     * HTTP Method: POST
     * Endpoint: /api/users/{userId}/social-links
     * @param userId Linkin ait olduğu kullanıcının ID'si (URL yolundan alınır).
     * @param socialLinkRequest Link bilgilerini içeren SocialLinkRequest DTO.
     * @return Oluşturulan link için SocialLinkResponse objesi ve 201 Created durumu.
     */
    @PostMapping
    public ResponseEntity<SocialLinkResponse> createSocialLink(
            @PathVariable Long userId,
            @Valid @RequestBody SocialLinkRequest socialLinkRequest) {
        log.info("Kullanıcı ID: {} için sosyal link oluşturma isteği alındı.", userId);
        // Güvenlik kontrolü: İstek yapan kullanıcının kendi profili mi olduğunu doğrula
        validateUserOwnership(userId); 
        SocialLinkResponse createdSocialLink = socialLinkService.createSocialLink(userId, socialLinkRequest);
        log.info("Kullanıcı ID: {} için sosyal link başarıyla oluşturuldu: {}", userId, createdSocialLink.getPlatform());
        return new ResponseEntity<>(createdSocialLink, HttpStatus.CREATED);
    }

    /**
     * Belirli bir ID'ye sahip sosyal medya linkini getirir.
     * HTTP Method: GET
     * Endpoint: /api/users/{userId}/social-links/{socialLinkId}
     * @param socialLinkId Sosyal linkin benzersiz ID'si (URL yolundan alınır).
     * @return Bulunan link için SocialLinkResponse objesi ve 200 OK durumu, veya 404 Not Found.
     */
    @GetMapping("/{socialLinkId}") // KRİTİK DÜZELTME: Path değişkeni socialLinkId olarak güncellendi
    public ResponseEntity<SocialLinkResponse> getSocialLinkById(@PathVariable Long socialLinkId) { // KRİTİK DÜZELTME: Parametre adı socialLinkId olarak güncellendi
        log.info("Sosyal Link ID: {} için getirme isteği alındı.", socialLinkId);
        SocialLinkResponse socialLinkResponse = socialLinkService.getSocialLinkById(socialLinkId);
        log.info("Sosyal Link ID: {} başarıyla getirildi: {}", socialLinkId, socialLinkResponse.getPlatform());
        return ResponseEntity.ok(socialLinkResponse);
    }

    /**
     * Belirli bir kullanıcıya ait tüm sosyal medya linklerini listeler.
     * HTTP Method: GET
     * Endpoint: /api/users/{userId}/social-links
     * @param userId Sosyal linklerin ait olduğu kullanıcının ID'si (URL yolundan alınır).
     * @return SocialLinkResponse objelerinin listesi ve 200 OK durumu.
     */
    @GetMapping
    public ResponseEntity<List<SocialLinkResponse>> getAllSocialLinksByUserId(@PathVariable Long userId) { // KRİTİK DÜZELTME: Metot adı 'getAllSocialLinksByUserId' olarak değiştirildi
        log.info("Kullanıcı ID: {} için tüm sosyal linkleri getirme isteği alındı.", userId);
        // Güvenlik kontrolü: İstek yapan kullanıcının kendi profili mi olduğunu doğrula
        validateUserOwnership(userId);
        List<SocialLinkResponse> socialLinkResponses = socialLinkService.getAllSocialLinksByUserId(userId); // KRİTİK DÜZELTME: Metot adı güncellendi
        log.info("Kullanıcı ID: {} için {} sosyal link bulundu.", userId, socialLinkResponses.size());
        return ResponseEntity.ok(socialLinkResponses);
    }

    /**
     * Mevcut bir sosyal medya linkini günceller.
     * HTTP Method: PUT
     * Endpoint: /api/users/{userId}/social-links/{socialLinkId}
     * @param userId Linkin ait olduğu kullanıcının ID'si (URL yolundan alınır).
     * @param socialLinkId Güncellenecek sosyal linkin benzersiz ID'si (URL yolundan alınır).
     * @param socialLinkRequest Güncelleme bilgilerini içeren SocialLinkRequest DTO.
     * @return Güncellenen link için SocialLinkResponse objesi ve 200 OK durumu.
     */
    @PutMapping("/{socialLinkId}") // KRİTİK DÜZELTME: Path değişkeni socialLinkId olarak güncellendi
    public ResponseEntity<SocialLinkResponse> updateSocialLink(
            @PathVariable Long userId,
            @PathVariable Long socialLinkId, // KRİTİK DÜZELTME: Parametre adı socialLinkId olarak güncellendi
            @Valid @RequestBody SocialLinkRequest socialLinkRequest) {
        log.info("Kullanıcı ID: {} ve Sosyal Link ID: {} için güncelleme isteği alındı.", userId, socialLinkId);
        // Güvenlik kontrolü: İstek yapan kullanıcının kendi profili mi olduğunu doğrula
        validateUserOwnership(userId);
        SocialLinkResponse updatedLink = socialLinkService.updateSocialLink(userId, socialLinkId, socialLinkRequest);
        log.info("Kullanıcı ID: {} ve Sosyal Link ID: {} başarıyla güncellendi: {}", userId, socialLinkId, updatedLink.getPlatform());
        return ResponseEntity.ok(updatedLink);
    }

    /**
     * Belirli bir ID'ye sahip sosyal medya linkini siler.
     * HTTP Method: DELETE
     * Endpoint: /api/users/{userId}/social-links/{socialLinkId}
     * @param userId Linkin ait olduğu kullanıcının ID'si (URL yolundan alınır).
     * @param socialLinkId Silinecek sosyal linkin benzersiz ID'si.
     * @return 204 No Content durumu (başarılı silme durumunda).
     */
    @DeleteMapping("/{socialLinkId}") // KRİTİK DÜZELTME: Path değişkeni socialLinkId olarak güncellendi
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content döner
    public void deleteSocialLink(
            @PathVariable Long userId, // KRİTİK DÜZELTME: userId eklendi
            @PathVariable Long socialLinkId) { // KRİTİK DÜZELTME: socialLinkId eklendi
        log.info("Kullanıcı ID: {} ve Sosyal Link ID: {} için silme isteği alındı.", userId, socialLinkId);
        // Güvenlik kontrolü: İstek yapan kullanıcının kendi profili mi olduğunu doğrula
        validateUserOwnership(userId);
        socialLinkService.deleteSocialLink(userId, socialLinkId); // KRİTİK DÜZELTME: userId ve socialLinkId birlikte gönderiliyor
        log.info("Kullanıcı ID: {} ve Sosyal Link ID: {} başarıyla silindi.", userId, socialLinkId);
    }

    /**
     * Kimliği doğrulanmış kullanıcının, path değişkenindeki userId ile eşleşip eşleşmediğini kontrol eder.
     * Eşleşmiyorsa AccessDeniedException fırlatır.
     * @param pathUserId URL'den gelen kullanıcı ID'si.
     */
    private void validateUserOwnership(Long pathUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Kimlik doğrulaması yapılmamış veya anonim kullanıcıdan yetkisiz erişim denemesi.");
            throw new AccessDeniedException("Kimlik doğrulaması yapılmamış kullanıcı.");
        }

        Object principal = authentication.getPrincipal();
        Long authenticatedUserId;

        // UserDetailsImpl sınıfınızın doğru paketi kullanıldı.
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
