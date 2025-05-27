package com.soliner.digitalcard.webApi.dto.project;

import lombok.Data;

/**
 * Proje bilgilerini API üzerinden döndürmek için kullanılan veri transfer nesnesi (DTO).
 * Proje ID'si, başlığı, açıklaması, URL'si, teknolojileri ve ilişkili kullanıcı ID'si bilgilerini içerir.
 */
@Data // Lombok: Otomatik olarak getter, setter, equals, hashCode ve toString metodlarını oluşturur.
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String projectUrl;
    private String technologies;
    private Long userId;
}
