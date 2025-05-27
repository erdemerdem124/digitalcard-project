package com.soliner.digitalcard.webApi.security;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security'nin kimlik doğrulama hatalarını (AuthenticationException) yakalayan giriş noktası.
 * Yetkilendirilmemiş bir istek geldiğinde (örneğin, JWT token olmadan korumalı bir kaynağa erişim),
 * bu sınıf tetiklenir ve istemciye uygun bir hata yanıtı döner.
 * webApi katmanına aittir çünkü web güvenliği ile doğrudan ilgilenir.
 */
@Component // Spring tarafından yönetilen bir bileşen olduğunu belirtir
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * Kimlik doğrulama hatası oluştuğunda tetiklenir.
     * @param request HTTP isteği.
     * @param response HTTP yanıtı.
     * @param authException Kimlik doğrulama hatası nesnesi.
     * @throws IOException G/Ç hataları.
     * @throws ServletException Servlet hataları.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Yetkilendirilmemiş hata: {}", authException.getMessage());
        // HTTP 401 Unauthorized durum kodu ile yanıt döner
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Hata: Yetkilendirilmemiş");
    }
}
