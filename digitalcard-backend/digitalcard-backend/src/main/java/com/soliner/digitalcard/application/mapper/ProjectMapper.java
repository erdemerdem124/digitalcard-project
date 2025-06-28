package com.soliner.digitalcard.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Context;

import com.soliner.digitalcard.domain.model.Project;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface ProjectMapper {

    // 🔥 KRİTİK EKLENTİ: UserServiceImpl'in ihtiyaç duyduğu tek parametreli toEntity metodu
    // Bu metot, ProjectRequest'ten Project entity'si oluşturur, ancak user ilişkisini
    // manuel olarak servis katmanında kurulmasını bekler.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // User ilişkisi servis katmanında kurulacak
    @Mapping(source = "projectImageUrl", target = "projectImageUrl") // DTO'dan Entity'e aynı isimle mapleniyorsa açıkça belirtmek daha iyi
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(ProjectRequest projectRequest);


    // Mevcut çok parametreli toEntity metodu (örn. UserMapper'dan @Context ile çağrılabilir)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(user)")
    @Mapping(source = "projectImageUrl", target = "projectImageUrl") // DTO'dan Entity'e
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(ProjectRequest projectRequest, @Context User user);


    @Mapping(source = "user.id", target = "userId")
    // KRİTİK DÜZELTME: projectImageUrl zaten Project entity'sinde de aynı isimde olmalı,
    // o yüzden source ve target'ı ayrı ayrı belirtmek yerine doğrudan bırakılabilir.
    // Eğer entity'de farklı bir isimdeyse, source'u entity'deki ismiyle belirtin.
    // Şimdiki haliyle Project entity'sinde de 'projectImageUrl' olduğunu varsayıyoruz.
    ProjectResponse toResponse(Project project);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "projectImageUrl", target = "projectImageUrl") // DTO'dan Entity'ye
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProjectRequest projectRequest, @MappingTarget Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);

    // 🔥 EKSİK OLAN METOT: LIST DÖNÜŞÜMÜ (Artık ihtiyacımız yok çünkü stream().map() kullanıyoruz)
    // toEntityList(List<ProjectRequest> projectRequests, @Context User user); // Bu metodu kaldırdık
}
