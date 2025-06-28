package com.soliner.digitalcard.webApi.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Proje bilgilerini API üzerinden döndürmek için kullanılan veri transfer nesnesi (DTO).
 * Proje ID'si, başlığı, açıklaması, URL'si, teknolojileri ve ilişkili kullanıcı ID'si bilgilerini içerir.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    
    @JsonProperty("project_url") // KRİTİK DÜZELTME: request ile tutarlılık için project_url olarak ayarlandı
    private String projectUrl;
    
    private String technologies;
    
    @JsonProperty("project_image_url") // KRİTİK DÜZELTME: request ile tutarlılık için project_image_url olarak ayarlandı
    private String projectImageUrl; 
    
    private Long userId;
}
