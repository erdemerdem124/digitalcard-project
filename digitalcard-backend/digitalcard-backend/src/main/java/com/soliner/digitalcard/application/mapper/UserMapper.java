package com.soliner.digitalcard.application.mapper;

import com.soliner.digitalcard.domain.model.Project;
import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors; // Collectors için import

@Mapper(componentModel = "spring",
        uses = {SocialLinkMapper.class, ProjectMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class UserMapper {

    @Autowired
    protected SocialLinkMapper socialLinkMapper;

    @Autowired
    protected ProjectMapper projectMapper;

    // UserRequest (frontend'den gelen) -> User entity'sine dönüşüm (createUser için)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "roles", ignore = true) // Roller manuel atanıyor
    @Mapping(source = "location", target = "address")
    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(source = "portfolioUrl", target = "website")
    @Mapping(source = "profileImageUrl", target = "profilePhotoUrl")
    // Bu metodda socialLinks ve projects mapping'leri kalmalı çünkü createUser'da entity ilk kez oluşturuluyor
    @Mapping(target = "socialLinks", expression = "java(userRequest.getSocialLinks() != null ? userRequest.getSocialLinks().stream().map(req -> socialLinkMapper.toEntity(req)).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    @Mapping(target = "projects", expression = "java(userRequest.getProjects() != null ? userRequest.getProjects().stream().map(req -> projectMapper.toEntity(req)).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    public abstract User toUser(UserRequest userRequest);

    // User entity'sinden -> UserResponse (frontend'e giden) dönüşüm
    @Mapping(source = "address", target = "location")
    @Mapping(source = "phoneNumber", target = "phone")
    @Mapping(source = "website", target = "portfolioUrl")
    @Mapping(source = "profilePhotoUrl", target = "profileImageUrl")
    @Mapping(target = "roles", expression = "java(user.getRoles() != null ? user.getRoles().stream().map(role -> role.getName().name()).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    @Mapping(target = "socialLinks", expression = "java(user.getSocialLinks() != null ? user.getSocialLinks().stream().map(socialLinkMapper::toResponse).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    @Mapping(target = "projects", expression = "java(user.getProjects() != null ? user.getProjects().stream().map(projectMapper::toResponse).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    public abstract UserResponse toResponse(User user);

    // UserRequest (frontend'den gelen) -> Mevcut User entity'sine güncelleme (updateUser için)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true) // updatedAt UserService'de manuel güncellenecek
    @Mapping(source = "location", target = "address")
    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(source = "portfolioUrl", target = "website")
    @Mapping(source = "profileImageUrl", target = "profilePhotoUrl")
    // KRİTİK DÜZELTME: updateEntityFromDto metodunda socialLinks ve projects alanlarını tamamen ignore et!
    // Bu koleksiyonların yönetimi artık UserServiceImpl'de manuel olarak yapılacak.
    @Mapping(target = "socialLinks", ignore = true) // MapStruct'ın bu koleksiyona dokunmasını engelle
    @Mapping(target = "projects", ignore = true) // MapStruct'ın bu koleksiyona dokunmasını engelle
    public abstract void updateEntityFromDto(UserRequest userRequest, @MappingTarget User user);

    public abstract List<SocialLink> toSocialLinkEntityList(List<SocialLinkRequest> socialLinkRequests);
    public abstract List<Project> toProjectEntityList(List<ProjectRequest> projectRequests);
    
    public abstract List<UserResponse> toResponseList(List<User> users);
}
