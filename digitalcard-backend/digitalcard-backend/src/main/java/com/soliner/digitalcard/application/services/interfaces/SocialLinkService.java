package com.soliner.digitalcard.application.services.interfaces;

import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;

import java.util.List;

/**
 * Sosyal Link iş mantığı operasyonları için arayüz.
 * Bu arayüz, webApi katmanındaki Controller'lar tarafından çağrılır.
 * application katmanına aittir.
 */
public interface SocialLinkService {

    // Yeni bir sosyal link oluşturma metodu (userId ve SocialLinkRequest alıp SocialLinkResponse döndürüyor)
    SocialLinkResponse createSocialLink(Long userId, SocialLinkRequest socialLinkRequest);

    // Mevcut bir sosyal linki güncelleme metodu (userId, socialLinkId ve SocialLinkRequest alıp SocialLinkResponse döndürüyor)
    SocialLinkResponse updateSocialLink(Long userId, Long socialLinkId, SocialLinkRequest socialLinkRequest); // KRİTİK DÜZELTME: id yerine socialLinkId

    // Belirli bir ID'ye sahip sosyal linki getirme metodu (SocialLinkResponse döndürüyor)
    SocialLinkResponse getSocialLinkById(Long socialLinkId); // KRİTİK DÜZELTME: id yerine socialLinkId

    // Belirli bir kullanıcıya ait tüm sosyal linkleri getirme metodu (List<SocialLinkResponse> döndürüyor)
    List<SocialLinkResponse> getAllSocialLinksByUserId(Long userId); // KRİTİK DÜZELTME: Metot adı 'getAllSocialLinksByUserId' olarak değiştirildi

    // Belirli bir ID'ye sahip sosyal linki, belirli bir kullanıcının silme metodu
    void deleteSocialLink(Long userId, Long socialLinkId); // KRİTİK DÜZELTME: userId ve socialLinkId parametreleri eklendi

    // NOT: getAllSocialLinks() metodu, global bir listeleme ihtiyacı olmadığı varsayılarak kaldırıldı.
    // Eğer yönetici paneli gibi bir ihtiyacınız varsa daha sonra eklenebilir.
}
