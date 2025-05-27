package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.mapper.SocialLinkMapper;
import com.soliner.digitalcard.application.services.interfaces.SocialLinkService;
import com.soliner.digitalcard.application.services.interfaces.UserService; // UserService'i import edin
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.SocialLinkRepository;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SocialLinkService arayüzünün implementasyonu.
 * Sosyal Link iş mantığı operasyonlarını gerçekleştirir ve Repository aracılığıyla veri erişimi sağlar.
 * application katmanına aittir.
 */
@Service
public class SocialLinkServiceImpl implements SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final SocialLinkMapper socialLinkMapper;
    private final UserService userService; // UserRepository yerine UserService enjekte edildi

    public SocialLinkServiceImpl(SocialLinkRepository socialLinkRepository, SocialLinkMapper socialLinkMapper, UserService userService) {
        this.socialLinkRepository = socialLinkRepository;
        this.socialLinkMapper = socialLinkMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public SocialLinkResponse createSocialLink(SocialLinkRequest request) {
        // 1. Kullanıcıyı bulma (UserService aracılığıyla)
        User user = userService.getUserById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", request.getUserId()));

        // 2. DTO'dan Entity'ye dönüşüm
        SocialLink socialLink = socialLinkMapper.toEntity(request);
        socialLink.setUser(user); // İlişkili kullanıcıyı set et

        // 3. Entity'yi kaydetme
        SocialLink savedLink = socialLinkRepository.save(socialLink);

        // 4. Kaydedilen Entity'den Response DTO'ya dönüşüm ve döndürme
        return socialLinkMapper.toResponse(savedLink);
    }

    @Override
    @Transactional
    public SocialLinkResponse updateSocialLink(Long id, SocialLinkRequest request) {
        // 1. Mevcut sosyal linki bulma
        SocialLink existingLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sosyal Link", "ID", id));

        // 2. Kullanıcı ID'si değişiyorsa veya geçerli değilse kontrol et ve güncelle
        // Not: Genellikle bir sosyal linkin kullanıcısı değişmez.
        // Eğer bu iş kuralı ise, aşağıdaki if bloğunu kaldırabilirsiniz.
        // Eğer değişebilir ve InvalidInputException fırlatmak istiyorsanız, onu da ekleyebilirsiniz.
        if (request.getUserId() != null &&
            (existingLink.getUser() == null || !existingLink.getUser().getId().equals(request.getUserId()))) {
            User newUser = userService.getUserById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", request.getUserId()));
            existingLink.setUser(newUser);
        }

        // 3. DTO'daki verileri mevcut Entity üzerine güncelleme
        socialLinkMapper.updateEntityFromDto(request, existingLink);

        // 4. Güncellenen Entity'yi kaydetme
        SocialLink updatedLink = socialLinkRepository.save(existingLink);

        // 5. Güncellenen Entity'den Response DTO'ya dönüşüm ve döndürme
        return socialLinkMapper.toResponse(updatedLink);
    }

    @Override
    public SocialLinkResponse getSocialLinkById(Long id) {
        // 1. Sosyal linki bulma
        SocialLink socialLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sosyal Link", "ID", id));

        // 2. Entity'den Response DTO'ya dönüşüm ve döndürme
        return socialLinkMapper.toResponse(socialLink);
    }

    @Override
    public List<SocialLinkResponse> getSocialLinksByUserId(Long userId) {
        // 1. Kullanıcının varlığını kontrol etme (iyi bir pratik)
        // Eğer kullanıcı yoksa, ona ait linkler de olamaz.
        userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", userId));

        // 2. Kullanıcıya ait sosyal linkleri Repository'den çekme
        List<SocialLink> socialLinks = socialLinkRepository.findByUserId(userId);

        // 3. Entity listesinden Response DTO listesine dönüşüm ve döndürme
        return socialLinks.stream()
                .map(socialLinkMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSocialLink(Long id) {
        // 1. Sosyal linkin varlığını kontrol etme
        if (!socialLinkRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sosyal Link", "ID", id);
        }
        // 2. Sosyal linki silme
        socialLinkRepository.deleteById(id);
    }

    // Arayüze eklediğimiz getAllSocialLinks() metodu için implementasyon
    @Override
    public List<SocialLinkResponse> getAllSocialLinks() {
        return socialLinkRepository.findAll().stream()
                .map(socialLinkMapper::toResponse)
                .collect(Collectors.toList());
    }
}