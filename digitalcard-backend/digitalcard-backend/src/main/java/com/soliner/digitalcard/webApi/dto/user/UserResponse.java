package com.soliner.digitalcard.webApi.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soliner.digitalcard.webApi.dto.project.ProjectResponse;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kullanıcı bilgilerinin API üzerinden döndürmek için kullanılan veri transfer nesnesi (DTO).
 * Bu DTO'nun Java alan adları, frontend'in beklediği JSON anahtarları ile tam olarak eşleşir.
 * Hassas bilgiler (örn. şifre) burada yer almaz.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    // KRİTİK DÜZELTME: Bu alanın Java adı da 'profileImageUrl' oldu.
    // Frontend'in beklediği isimle birebir aynı. Jackson bu Java adını JSON'a da aynı şekilde çevirir.
    // Bu durumda @JsonProperty'ye aslında gerek yoktur, ancak açıkça belirtmek için bırakıyorum.
    @JsonProperty("profileImageUrl") 
    private String profileImageUrl; // Java field name: profileImageUrl

    private String bio;
    private String title;

    // KRİTİK DÜZELTME: Bu alanın Java adı da 'location' oldu.
    // Frontend'in beklediği isimle birebir aynı.
    @JsonProperty("location") 
    private String location; // Java field name: location

    // KRİTİK DÜZELTME: Bu alanın Java adı da 'phone' oldu.
    // Frontend'in beklediği isimle birebir aynı.
    @JsonProperty("phone")
    private String phone; // Java field name: phone

    // KRİTİK DÜZELTME: Bu alanın Java adı da 'portfolioUrl' oldu.
    // Frontend'in beklediği isimle birebir aynı.
    @JsonProperty("portfolioUrl")
    private String portfolioUrl; // Java field name: portfolioUrl

    private List<String> roles;

    private List<SocialLinkResponse> socialLinks;
    private List<ProjectResponse> projects;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
