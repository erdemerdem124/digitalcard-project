package com.soliner.digitalcard.webApi.dto.project;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;


@Data // Lombok: Otomatik olarak getter, setter, equals, hashCode ve toString metodlarını oluşturur.
public class ProjectRequest {
   @NotBlank(message = "Proje başlığı boş olamaz") // Proje başlığının boş veya sadece boşluklardan oluşmamasını sağlar.
   @Size(max = 255, message = "Proje başlığı 255 karakterden uzun olamaz") // Proje başlığının maksimum uzunluğu.
   private String title;

   @Size(max = 2000, message = "Açıklama 2000 karakterden uzun olamaz") // Açıklama metninin maksimum uzunluğu.
   private String description;

   @Size(max = 500, message = "Proje URL'si 500 karakterden uzun olamaz") // Proje URL'sinin maksimum uzunluğu.
   private String projectUrl;

   @Size(max = 255, message = "Teknolojiler 255 karakterden uzun olamaz") // Kullanılan teknolojiler metninin maksimum uzunluğu.
   private String technologies;

   @NotNull(message = "Kullanıcı ID'si boş olamaz") // Bu projenin hangi kullanıcıya ait olduğunu belirten kullanıcı ID'si. Boş olamaz.
   private Long userId;
}
