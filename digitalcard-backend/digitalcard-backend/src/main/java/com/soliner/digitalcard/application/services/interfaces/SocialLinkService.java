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
    SocialLinkResponse createSocialLink(SocialLinkRequest socialLinkRequest);
    SocialLinkResponse updateSocialLink(Long id, SocialLinkRequest socialLinkRequest);
    SocialLinkResponse getSocialLinkById(Long id);
    List<SocialLinkResponse> getSocialLinksByUserId(Long userId);
    void deleteSocialLink(Long id);

    // *** YENİ EKLENEN METOT ***
    List<SocialLinkResponse> getAllSocialLinks();
}