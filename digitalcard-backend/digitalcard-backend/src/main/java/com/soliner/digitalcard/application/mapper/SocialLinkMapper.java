package com.soliner.digitalcard.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy; // Bu import'u ekleyin

import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;

/**
 * SocialLink Entity'si ile SocialLinkRequest ve SocialLinkResponse DTO'ları arasında dönüşüm sağlayan MapStruct Mapper arayüzü.
 */
@Mapper(componentModel = "spring",
        // Null olan kaynak özelliklerinin hedef özelliklere maplenmesini engeller.
        // Ancak nested property'ler için (user.id gibi) expression kullanmak daha güvenlidir.
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SocialLinkMapper {

    // SocialLinkRequest DTO'dan SocialLink Entity'ye dönüşüm
    // ID otomatik oluşturulacak
    @Mapping(target = "id", ignore = true)
    // User ilişkisi servis katmanında set edilecek, bu yüzden burada ignore ediyoruz.
    @Mapping(target = "user", ignore = true)
    SocialLink toEntity(SocialLinkRequest socialLinkRequest);

    // SocialLink Entity'den SocialLinkResponse DTO'ya dönüşüm
    // user.id'yi userId'ye maplerken, user null ise NullPointerException almamak için kontrol ekle
    @Mapping(target = "userId", expression = "java(socialLink.getUser() != null ? socialLink.getUser().getId() : null)")
    SocialLinkResponse toResponse(SocialLink socialLink);

    // SocialLinkRequest DTO'daki verileri mevcut SocialLink Entity üzerine güncelleme
    // ID güncellenmez
    @Mapping(target = "id", ignore = true)
    // User ilişkisi servis katmanında yönetilir
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(SocialLinkRequest socialLinkRequest, @MappingTarget SocialLink socialLink);
}
