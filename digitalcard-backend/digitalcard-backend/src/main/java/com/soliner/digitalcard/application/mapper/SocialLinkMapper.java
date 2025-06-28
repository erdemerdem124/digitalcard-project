package com.soliner.digitalcard.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Context; // Context için import

import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface SocialLinkMapper {

    // 🔥 KRİTİK EKLENTİ: UserServiceImpl'in ihtiyaç duyduğu tek parametreli toEntity metodu
    // Bu metot, SocialLinkRequest'ten SocialLink entity'si oluşturur, ancak user ilişkisini
    // manuel olarak servis katmanında kurulmasını bekler.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // User ilişkisi servis katmanında kurulacak
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SocialLink toEntity(SocialLinkRequest socialLinkRequest);


    // Mevcut çok parametreli toEntity metodu (örn. UserMapper'dan @Context ile çağrılabilir)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(user)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SocialLink toEntity(SocialLinkRequest socialLinkRequest, @Context User user); // Bu metot hala burada kalabilir


    @Mapping(source = "user.id", target = "userId")
    SocialLinkResponse toResponse(SocialLink socialLink);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SocialLinkRequest socialLinkRequest, @MappingTarget SocialLink socialLink);

    List<SocialLinkResponse> toResponseList(List<SocialLink> socialLinks);
    // 🔥 EKSİK OLAN METHOD: toEntityList (eğer doğrudan liste dönüşümü gerekiyorsa)
    // Ancak UserServiceImpl'de stream().map(socialLinkMapper::toEntity) kullanıldığı için
    // toEntityList metoduna doğrudan ihtiyacımız kalmıyor.
}
