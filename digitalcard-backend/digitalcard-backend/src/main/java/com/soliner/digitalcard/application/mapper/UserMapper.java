package com.soliner.digitalcard.application.mapper;

import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import com.soliner.digitalcard.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy; // NullValuePropertyMappingStrategy import edildi



@Mapper(componentModel = "spring",
        uses = { SocialLinkMapper.class, ProjectMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    // toEntity metodu için eklenen @Mapping anotasyonları
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "roles", ignore = true) // Roller de burada ignore edilmeli, manuel set edilecek
    User toEntity(UserRequest userRequest);

    // toResponse metodu için eklenen @Mapping anotasyonu
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // Şifre hash'i manuel olarak servis katmanında güncellenmelidir
    void updateEntityFromDto(UserRequest userRequest, @MappingTarget User user);
}