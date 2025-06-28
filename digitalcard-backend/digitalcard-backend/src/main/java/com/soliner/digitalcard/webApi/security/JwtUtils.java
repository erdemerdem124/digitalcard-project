package com.soliner.digitalcard.webApi.security;

import io.jsonwebtoken.Claims; // Bu import artık doğrudan kullanılmasa da, API'de kalabilir
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders; // Base64 decode için
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

import com.soliner.digitalcard.application.services.impl.UserDetailsImpl;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets; // Bu import artık doğrudan kullanılmasa da, kalabilir.
import java.util.Date;
import java.util.function.Function; // Bu import artık doğrudan kullanılmasa da, kalabilir.

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
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        // KRİTİK DÜZELTME: SecretKey oluşturma yöntemini key() metodu ile aynı hale getirdik.
        // Artık jwtSecret'in Base64 kodlu olduğu varsayılıyor.
        SecretKey secretKey = key(); 
        
        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // Claims.SUBJECT yerine subject() metodu
                .issuedAt(new Date())                // Claims.ISSUED_AT yerine issuedAt() metodu
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Claims.EXPIRATION yerine expiration() metodu
                .signWith(secretKey) // Güncellenmiş SecretKey kullanılıyor
                .compact();
    }

    /**
     * JWT token'dan kullanıcı adını (subject) çıkarır.
     * JJWT 0.12.x API'sine göre güncellendi.
     * @param token JWT token'ı.
     * @return Token'daki kullanıcı adı.
     */
    public String getUserNameFromJwtToken(String token) {
        String username = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
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
                    .verifyWith(key())
                    .build()
                    .parse(authToken);
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
        } catch (SignatureException e) { // Bu hata artık daha spesifik olarak imzayı yakalar
            logger.error("validateJwtToken - JWT imza doğrulaması başarısız: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT imzalama için kullanılacak gizli anahtarı döndürür.
     * Bu metod, `jwtSecret` değerini Base64 olarak çözerek SecretKey objesi oluşturur.
     * @return Gizli anahtar (SecretKey).
     */
    private SecretKey key() {
        // Bu kısım zaten Base64 decoding yapıyordu, bu haliyle korunuyor.
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
