package com.soliner.digitalcard.webApi.controller;

import com.soliner.digitalcard.application.services.interfaces.SocialLinkService; // Application katmanındaki servis
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest; // webApi katmanındaki DTO
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse; // webApi katmanındaki DTO
// Kaldırılan import'lar:
// import com.soliner.digitalcard.application.mapper.SocialLinkMapper; // Controller'da mapper'a gerek yok
// import com.soliner.digitalcard.domain.model.SocialLink; // Controller'da domain entity'ye gerek yok
// import com.soliner.digitalcard.domain.model.User; // Controller'da domain entity'ye gerek yok
// import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException; // Servis katmanı fırlatır, GlobalExceptionHandler yakalar

import jakarta.validation.Valid;
// import org.springframework.beans.factory.annotation.Autowired; // Constructor injection için genellikle gereksiz
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Optional; // Servis DTO döndürdüğü için Controller'da Optional'a gerek kalmadı
// import java.util.stream.Collectors; // Servis DTO listesi döndürdüğü için Controller'da stream'e gerek kalmadı

/**
 * Sosyal medya linkleri ile ilgili RESTful API endpoint'leri.
 * Gelen HTTP isteklerini karşılar, servis katmanını çağırır ve yanıtları DTO'lar aracılığıyla döndürür.
 * webApi katmanına aittir.
 */
@RestController // Bu sınıfın bir REST Controller olduğunu belirtir.
@RequestMapping("/api/sociallinks") // Tüm endpoint'ler için temel URL yolu (örn. /api/sociallinks).
public class SocialLinkController {

    private final SocialLinkService socialLinkService; // Sosyal link iş mantığı için servis katmanı.
    // Kaldırılan bağımlılıklar:
    // private final SocialLinkMapper socialLinkMapper;
    // private final UserService userService;

    // Constructor Injection. Artık sadece SocialLinkService'e bağımlı.
    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
        // this.socialLinkMapper = socialLinkMapper; // Kaldırıldı
        // this.userService = userService; // Kaldırıldı
    }

    /**
     * Belirli bir kullanıcıya ait tüm sosyal medya linklerini listeler.
     * HTTP Method: GET
     * Endpoint: /api/sociallinks/user/{userId}
     * @param userId Sosyal linklerin ait olduğu kullanıcının ID'si.
     * @return SocialLinkResponse objelerinin listesi ve 200 OK durumu.
     */
    @GetMapping("/user/{userId}") // Endpoint güncellendi: tüm linkleri listeleme yerine kullanıcıya ait linkleri listeleme
    public ResponseEntity<List<SocialLinkResponse>> getSocialLinksByUserId(@PathVariable Long userId) {
        // Servis katmanı zaten DTO listesi döndürüyor, Controller'da dönüşüme gerek yok.
        List<SocialLinkResponse> socialLinkResponses = socialLinkService.getSocialLinksByUserId(userId);
        return ResponseEntity.ok(socialLinkResponses); // 200 OK yanıtı döndür.
    }

    /**
     * Belirli bir ID'ye sahip sosyal medya linkini getirir.
     * HTTP Method: GET
     * Endpoint: /api/sociallinks/{id}
     * @param id Sosyal linkin benzersiz ID'si.
     * @return Bulunan link için SocialLinkResponse objesi ve 200 OK durumu, veya 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SocialLinkResponse> getSocialLinkById(@PathVariable Long id) {
        // Servis katmanı zaten DTO döndürüyor ve ResourceNotFoundException fırlatıyor.
        // GlobalExceptionHandler bu istisnayı 404'e çevirecek.
        SocialLinkResponse socialLinkResponse = socialLinkService.getSocialLinkById(id);
        return ResponseEntity.ok(socialLinkResponse);
    }

    /**
     * Yeni bir sosyal medya linki oluşturur.
     * HTTP Method: POST
     * Endpoint: /api/sociallinks
     * @param socialLinkRequest Link bilgilerini içeren SocialLinkRequest DTO.
     * @return Oluşturulan link için SocialLinkResponse objesi ve 201 Created durumu.
     */
    @PostMapping
    public ResponseEntity<SocialLinkResponse> createSocialLink(@Valid @RequestBody SocialLinkRequest socialLinkRequest) {
        // Controller sadece DTO'yu servise iletir. Kullanıcı kontrolü ve DTO-Entity dönüşümü servisin sorumluluğundadır.
        SocialLinkResponse createdSocialLink = socialLinkService.createSocialLink(socialLinkRequest);
        return new ResponseEntity<>(createdSocialLink, HttpStatus.CREATED);
    }

    /**
     * Mevcut bir sosyal medya linkini günceller.
     * HTTP Method: PUT
     * Endpoint: /api/sociallinks/{id}
     * @param id Güncellenecek sosyal linkin benzersiz ID'si.
     * @param socialLinkRequest Güncelleme bilgilerini içeren SocialLinkRequest DTO.
     * @return Güncellenen link için SocialLinkResponse objesi ve 200 OK durumu.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SocialLinkResponse> updateSocialLink(@PathVariable Long id, @Valid @RequestBody SocialLinkRequest socialLinkRequest) {
        // Controller sadece DTO'yu servise iletir. Güncelleme mantığı ve DTO-Entity dönüşümü servisin sorumluluğundadır.
        SocialLinkResponse updatedLink = socialLinkService.updateSocialLink(id, socialLinkRequest);
        return ResponseEntity.ok(updatedLink);
    }

    /**
     * Belirli bir ID'ye sahip sosyal medya linkini siler.
     * HTTP Method: DELETE
     * Endpoint: /api/sociallinks/{id}
     * @param id Silinecek sosyal linkin benzersiz ID'si.
     * @return 204 No Content durumu (başarılı silme durumunda).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content döner
    public void deleteSocialLink(@PathVariable Long id) {
        // Servis katmanı varlık kontrolünü ve silmeyi yapar. ResourceNotFoundException fırlatırsa
        // GlobalExceptionHandler 404'e çevirir.
        socialLinkService.deleteSocialLink(id);
    }
}
