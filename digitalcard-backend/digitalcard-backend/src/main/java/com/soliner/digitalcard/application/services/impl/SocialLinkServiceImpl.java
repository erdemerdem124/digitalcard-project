package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.mapper.SocialLinkMapper;
import com.soliner.digitalcard.application.services.interfaces.SocialLinkService;
import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.SocialLinkRepository;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors; // Eğer manuel stream kullanılıyorsa gerekli

/**
 * Sosyal bağlantı yönetimi iş mantığını uygulayan servis sınıfı.
 * application katmanına aittir.
 */
@Service
@Slf4j
public class SocialLinkServiceImpl implements SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final SocialLinkMapper socialLinkMapper;
    private final UserService userService;

    public SocialLinkServiceImpl(SocialLinkRepository socialLinkRepository, SocialLinkMapper socialLinkMapper, UserService userService) {
        this.socialLinkRepository = socialLinkRepository;
        this.socialLinkMapper = socialLinkMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public SocialLinkResponse createSocialLink(Long userId, SocialLinkRequest socialLinkRequest) {
        log.info("createSocialLink metodu çağrıldı. Kullanıcı ID: {}", userId);
        
        // KRİTİK DÜZELTME: UserService'den User objesini doğru şekilde alıyoruz.
        // userService.getUserById(userId) -> UserResponse döner
        // UserResponse.getUsername() -> Kullanıcı adını verir
        // userService.findByUsernameOrEmail() -> Optional<User> döner, var yok kontrolü ile User objesine ulaşılır.
        User user = userService.findByUsernameOrEmail(userService.getUserById(userId).getUsername())
                           .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", userId));

        // Kullanıcının bu platformda zaten bir linki olup olmadığını kontrol et
        if (socialLinkRepository.findByUserAndPlatform(user, socialLinkRequest.getPlatform()).isPresent()) {
            log.warn("Kullanıcının zaten bu platformda bir sosyal linki var: {} - Kullanıcı ID: {}", socialLinkRequest.getPlatform(), userId);
            throw new InvalidInputException("Platform", socialLinkRequest.getPlatform(), "Bu platformda bir sosyal linkiniz zaten mevcut.");
        }

        // KRİTİK DÜZELTME: socialLinkMapper.toEntity metoduna 'user' objesi de gönderildi
        SocialLink socialLink = socialLinkMapper.toEntity(socialLinkRequest, user);
        // socialLink.setUser(user); // Mapper'da zaten yapıldığı için bu satır gereksiz hale geldi
        SocialLink savedSocialLink = socialLinkRepository.save(socialLink);
        log.info("Sosyal link başarıyla kaydedildi: {} - Kullanıcı ID: {}", savedSocialLink.getPlatform(), userId);
        return socialLinkMapper.toResponse(savedSocialLink);
    }

    @Override
    @Transactional
    public SocialLinkResponse updateSocialLink(Long userId, Long socialLinkId, SocialLinkRequest socialLinkRequest) {
        log.info("updateSocialLink metodu çağrıldı. Kullanıcı ID: {}, Sosyal Link ID: {}", userId, socialLinkId);
        
        // KRİTİK DÜZELTME: UserService'den User objesini doğru şekilde alıyoruz.
        User user = userService.findByUsernameOrEmail(userService.getUserById(userId).getUsername())
                           .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", userId));

        SocialLink existingSocialLink = socialLinkRepository.findById(socialLinkId)
                .orElseThrow(() -> {
                    log.warn("Güncellenecek sosyal link bulunamadı. Sosyal Link ID: {}", socialLinkId);
                    return new ResourceNotFoundException("Sosyal Link", "ID", socialLinkId);
                });

        // Sosyal linkin doğru kullanıcıya ait olduğunu doğrula
        if (!existingSocialLink.getUser().getId().equals(userId)) {
            log.warn("Sosyal link, kullanıcıya ait değil. Sosyal Link ID: {}, Kullanıcı ID: {}", socialLinkId, userId);
            throw new InvalidInputException("Yetkilendirme Hatası", "Bu sosyal link size ait değil veya erişim yetkiniz yok.");
        }

        // Platform değiştiyse ve yeni platformda başka bir link mevcutsa kontrol et
        if (!existingSocialLink.getPlatform().equals(socialLinkRequest.getPlatform())) {
            if (socialLinkRepository.findByUserAndPlatform(user, socialLinkRequest.getPlatform()).isPresent()) {
                log.warn("Kullanıcının zaten bu platformda başka bir sosyal linki var: {} - Kullanıcı ID: {}", socialLinkRequest.getPlatform(), userId);
                throw new InvalidInputException("Platform", socialLinkRequest.getPlatform(), "Bu platformda başka bir sosyal linkiniz zaten mevcut.");
            }
        }

        socialLinkMapper.updateEntityFromDto(socialLinkRequest, existingSocialLink);
        SocialLink updatedSocialLink = socialLinkRepository.save(existingSocialLink);
        log.info("Sosyal link başarıyla güncellendi: {} - Kullanıcı ID: {}", updatedSocialLink.getPlatform(), userId);
        return socialLinkMapper.toResponse(updatedSocialLink);
    }

    @Override
    public SocialLinkResponse getSocialLinkById(Long socialLinkId) {
        log.info("getSocialLinkById metodu çağrıldı. Sosyal Link ID: {}", socialLinkId);
        SocialLink socialLink = socialLinkRepository.findById(socialLinkId)
                .orElseThrow(() -> {
                    log.warn("Sosyal link bulunamadı. Sosyal Link ID: {}", socialLinkId);
                    return new ResourceNotFoundException("Sosyal Link", "ID", socialLinkId);
                });
        log.info("Sosyal link bulundu: {}", socialLink.getPlatform());
        return socialLinkMapper.toResponse(socialLink);
    }

    @Override
    public List<SocialLinkResponse> getAllSocialLinksByUserId(Long userId) {
        log.info("getAllSocialLinksByUserId metodu çağrıldı. Kullanıcı ID: {}", userId);
        // Kullanıcının varlığını kontrol et (userService.getUserById zaten ResourceNotFoundException fırlatır)
        userService.getUserById(userId); 
        List<SocialLink> socialLinks = socialLinkRepository.findByUserId(userId);
        // KRİTİK DÜZELTME: Mapper'daki toResponseList metodunu kullanıyoruz
        List<SocialLinkResponse> socialLinkResponses = socialLinkMapper.toResponseList(socialLinks);
        log.info("Kullanıcı ID {} için {} sosyal link bulundu.", userId, socialLinkResponses.size());
        return socialLinkResponses;
    }

    @Override
    @Transactional
    public void deleteSocialLink(Long userId, Long socialLinkId) {
        log.info("deleteSocialLink metodu çağrıldı. Kullanıcı ID: {}, Sosyal Link ID: {}", userId, socialLinkId);
        SocialLink socialLink = socialLinkRepository.findById(socialLinkId)
                .orElseThrow(() -> {
                    log.warn("Silinecek sosyal link bulunamadı. Sosyal Link ID: {}", socialLinkId);
                    return new ResourceNotFoundException("Sosyal Link", "ID", socialLinkId);
                });

        if (!socialLink.getUser().getId().equals(userId)) {
            log.warn("Sosyal link, kullanıcıya ait değil. Sosyal Link ID: {}, Kullanıcı ID: {}", socialLinkId, userId);
            throw new InvalidInputException("Yetkilendirme Hatası", "Bu sosyal link size ait değil veya silme yetkiniz yok.");
        }
        
        socialLinkRepository.delete(socialLink);
        log.info("Sosyal link başarıyla silindi: {} - Kullanıcı ID: {}", socialLink.getPlatform(), userId);
    }
}
