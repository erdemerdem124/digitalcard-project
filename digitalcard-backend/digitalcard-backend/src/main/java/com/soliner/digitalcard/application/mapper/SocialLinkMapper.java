package com.soliner.digitalcard.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest; // Doğru import
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse; // Doğru import

/**
 * SocialLink Entity'si ile SocialLinkRequest ve SocialLinkResponse DTO'ları arasında dönüşüm sağlayan MapStruct Mapper arayüzü.
 */
@Mapper(componentModel = "spring")
public interface SocialLinkMapper {

    @Mapping(source = "userId", target = "user.id")
    SocialLink toEntity(SocialLinkRequest socialLinkRequest);

    @Mapping(source = "user.id", target = "userId")
    SocialLinkResponse toResponse(SocialLink socialLink);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(SocialLinkRequest socialLinkRequest, @MappingTarget SocialLink socialLink);
}