package com.soliner.digitalcard.webApi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // Key yerine SecretKey kullanıyoruz
import java.util.Date;

/**
 * JWT (JSON Web Token) oluşturma, doğrulama ve ayrıştırma işlemleri için yardımcı sınıf.
 * Bu sınıf, JWT ile ilgili tüm güvenlik operasyonlarını merkezi bir yerde toplar.
 * webApi katmanına aittir çünkü web güvenliği ile doğrudan ilgilenir.
 * JJWT 0.12.x ve üzeri sürümler için güncellenmiş API'leri kullanır.
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${soliner.app.jwtSecret}")
    private String jwtSecret;

    @Value("${soliner.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Kimliği doğrulanmış kullanıcıdan JWT token oluşturur.
     * JJWT 0.12.x API'sine göre güncellendi.
     * @param authentication Kimliği doğrulanmış kullanıcı bilgilerini içeren Authentication nesnesi.
     * @return Oluşturulan JWT token'ı.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        String jwt = Jwts.builder()
                .subject(userPrincipal.getUsername()) // setSubject yerine subject() kullanıldı
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs)) // setExpiration yerine expiration() kullanıldı
                .signWith(key()) // signWith metodu hala geçerli
                .compact();
        logger.debug("generateJwtToken - Oluşturulan JWT: {}", jwt);
        return jwt;
    }

    /**
     * JWT imzalama için kullanılacak gizli anahtarı döndürür.
     * @return Gizli anahtar (SecretKey).
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * JWT token'dan kullanıcı adını (subject) çıkarır.
     * JJWT 0.12.x API'sine göre güncellendi.
     * @param token JWT token'ı.
     * @return Token'daki kullanıcı adı.
     */
    public String getUserNameFromJwtToken(String token) {
        String username = Jwts.parser()
                .verifyWith(key()) // Yeni metot: setSigningKey yerine verifyWith
                .build()
                .parseSignedClaims(token) // Yeni metot: parseClaimsJws yerine parseSignedClaims
                .getPayload() // Yeni metot: getBody yerine getPayload
                .getSubject();
        logger.debug("getUserNameFromJwtToken - Token'dan ayrıştırılan kullanıcı adı: {}", username);
        return username;
    }

    /**
     * JWT token'ın geçerliliğini doğrular.
     * JJWT 0.12.x API'sine göre güncellendi.
     * @param authToken Doğrulanacak JWT token'ı.
     * @return Token geçerliyse true, aksi takdirde false.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key()) // Yeni metot: setSigningKey yerine verifyWith
                    .build()
                    .parse(authToken); // parseSignedClaims veya parse metodu kullanılabilir
            logger.debug("validateJwtToken - JWT token geçerli ve imzası doğru.");
            return true;
        } catch (MalformedJwtException e) {
            logger.error("validateJwtToken - Geçersiz JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("validateJwtToken - JWT token süresi doldu: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("validateJwtToken - Desteklenmeyen JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("validateJwtToken - JWT claims boş: {}", e.getMessage());
        } catch (SignatureException e) { // İmza doğrulama hatası için yeni eklenen catch bloğu
            logger.error("validateJwtToken - JWT imza doğrulaması başarısız: {}", e.getMessage());
        }
        return false;
    }
}
