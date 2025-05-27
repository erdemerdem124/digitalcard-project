	package com.soliner.digitalcard.webApi.dto.sociallink;

	import lombok.Data;

	/**
	 * Sosyal medya linki bilgilerini API üzerinden döndürmek için kullanılan veri transfer nesnesi (DTO).
	 * Link ID'si, platform adı, URL ve ilişkili kullanıcı ID'si bilgilerini içerir.
	 */
	@Data // Lombok: Otomatik olarak getter, setter, equals, hashCode ve toString metodlarını oluşturur.
	public class SocialLinkResponse {
	    private Long id;
	    private String platform;
	    private String url;
	    private Long userId; // Hangi kullanıcıya ait olduğu.
	}
