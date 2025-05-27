package com.soliner.digitalcard.webApi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

import com.soliner.digitalcard.webApi.dto.project.ProjectResponse;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkResponse;


/**
 * Kullanıcı bilgilerini API üzerinden döndürmek için kullanılan veri transfer nesnesi (DTO).
 * Kullanıcı ID'si, kullanıcı adı, profil fotoğrafı URL'si, biyografi ve ilişkili link/proje listelerini içerir.
 * Hassas bilgiler (örn. şifre) burada yer almaz.
 */
@Builder
@Data
@AllArgsConstructor // <-- BU SATIR EKLEDİM!
public class UserResponse {
	 private Long id;
	    private String username;
	    private String email; // Yeni eklenen alan
	    private String firstName; // Yeni eklenen alan
	    private String lastName; // Yeni eklenen alan
	    private String profilePhotoUrl;
	    private String bio;

    private List<SocialLinkResponse> socialLinks;
    private List<ProjectResponse> projects;
}
