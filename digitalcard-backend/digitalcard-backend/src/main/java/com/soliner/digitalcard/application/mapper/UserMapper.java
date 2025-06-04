package com.soliner.digitalcard.application.mapper;

import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import com.soliner.digitalcard.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = { SocialLinkMapper.class, ProjectMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface UserMapper {
    // toEntity metodu için eklenen @Mapping anotasyonları
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "roles", ignore = true) // Roller de burada ignore edilmeli, manuel set edilecek
    User toEntity(UserRequest userRequest);

    // BURASI GÜNCELLENDİ: toResponse metodu için profilePhotoUrl -> profileImageUrl eşlemesi eklendi
    @Mapping(source = "profilePhotoUrl", target = "profileImageUrl")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // Şifre hash'i manuel olarak servis katmanında güncellenmelidir
    @Mapping(source = "profileImageUrl", target = "profilePhotoUrl")
    void updateEntityFromDto(UserRequest userRequest, @MappingTarget User user);
}
