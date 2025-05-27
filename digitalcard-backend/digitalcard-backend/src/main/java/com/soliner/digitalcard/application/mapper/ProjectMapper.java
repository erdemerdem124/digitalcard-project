package com.soliner.digitalcard.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.soliner.digitalcard.domain.model.Project;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest; // BURASI GÜNCELLENDİ
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse; // BURASI GÜNCELLENDİ

/**
 * Project Entity'si ile ProjectRequest ve ProjectResponse DTO'ları arasında dönüşüm sağlayan MapStruct Mapper arayüzü.
 * MapStruct, bu arayüzden derleme zamanında otomatik olarak implementasyon oluşturur.
 */
@Mapper(componentModel = "spring") // Spring bileşeni olarak MapStruct'ın otomatik implementasyonunu sağlar
public interface ProjectMapper {

    /**
     * ProjectRequest DTO'sunu Project Entity'sine dönüştürür.
     * userId alanını Project Entity'sinin user objesine map'ler.
     * @param projectRequest Dönüştürülecek ProjectRequest nesnesi.
     * @return Dönüştürülmüş Project Entity nesnesi.
     */
    @Mapping(source = "userId", target = "user.id") // userId'yi User Entity'sinin id'sine mapler
    Project toEntity(ProjectRequest projectRequest);

    /**
     * Project Entity'sini ProjectResponse DTO'suna dönüştürür.
     * user objesindeki id'yi ProjectResponse'un userId alanına map'ler.
     * @param project Dönüştürülecek Project Entity nesnesi.
     * @return Dönüştürülmüş ProjectResponse DTO nesnesi.
     */
    @Mapping(source = "user.id", target = "userId") // User objesindeki id'yi userId'ye mapler
    ProjectResponse toResponse(Project project);

    /**
     * ProjectRequest DTO'sundaki verileri mevcut bir Project Entity nesnesine günceller.
     * ID ve user (ilişkili kullanıcı) alanlarının güncellenmemesini sağlar.
     * @param projectRequest Güncelleme verilerini içeren ProjectRequest nesnesi.
     * @param project Güncellenecek mevcut Project Entity nesnesi.
     */
    @Mapping(target = "id", ignore = true) // ID'nin güncellenmemesini sağlar
    @Mapping(target = "user", ignore = true) // İlişkili user objesinin doğrudan güncellenmemesini sağlar
    void updateEntityFromDto(ProjectRequest projectRequest, @MappingTarget Project project);
}