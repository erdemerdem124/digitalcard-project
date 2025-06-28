package com.soliner.digitalcard.webApi.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty; // JsonProperty eklendi

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    // Frontend'den 'project_url' olarak gelmesini bekliyoruz
    @JsonProperty("project_url")
    @Size(max = 255, message = "Project URL must be less than 255 characters")
    private String projectUrl; // Daha önce 'url' idi, Project entity'deki 'projectUrl' ile eşleşmesi için değiştirildi

    @Size(max = 255, message = "Technologies must be less than 255 characters")
    private String technologies; // Yeni eklendi

    // Frontend'den 'project_image_url' olarak gelmesini bekliyoruz
    @JsonProperty("project_image_url")
    @Size(max = 255, message = "Project image URL must be less than 255 characters")
    private String projectImageUrl; // Yeni eklendi
}
