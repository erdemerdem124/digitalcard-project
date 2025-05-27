package com.soliner.digitalcard.webApi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soliner.digitalcard.application.services.impl.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT (JSON Web Token) kimlik doğrulama filtresi.
 * Her gelen HTTP isteğini yakalar, istek başlığından JWT token'ı çıkarır,
 * token'ı doğrular ve geçerliyse Spring Security bağlamını ayarlar.
 * webApi katmanına aittir çünkü web güvenliği ile doğrudan ilgilenir.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // JWT doğrulaması gerektirmeyen (public) URL'ler
    private static final List<String> EXCLUDED_URLS = Arrays.asList(
            "/api/auth/**",
            "/api/public/**"
            // Buraya başka public URL'ler de eklenebilir
    );

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Belirli bir isteğin filtre tarafından atlanıp atlanmayacağını belirler.
     * Eğer istek, EXCLUDED_URLS listesindeki bir yola uyuyorsa, filtre çalışmaz.
     * @param request HTTP isteği.
     * @return İstek filtrelenmemesi gerekiyorsa true, aksi takdirde false.
     * @throws ServletException Servlet hataları.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestUri = request.getRequestURI();
        boolean shouldSkip = EXCLUDED_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));

        logger.info("shouldNotFilter - İstek URI: {}", requestUri);
        logger.info("shouldNotFilter - Atlanacak URL desenleri: {}", EXCLUDED_URLS);
        logger.info("shouldNotFilter - Eşleşme durumu (shouldSkip): {}", shouldSkip);

        return shouldSkip;
    }

    /**
     * Her gelen istek için filtreleme mantığını uygular.
     * @param request HTTP isteği.
     * @param response HTTP yanıtı.
     * @param filterChain Filtre zinciri.
     * @throws ServletException Servlet hataları.
     * @throws IOException G/Ç hataları.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            logger.debug("doFilterInternal - İstek URI: {}", request.getRequestURI());

            String jwt = parseJwt(request);
            logger.debug("doFilterInternal - Ayrıştırılan JWT: {}", jwt != null ? "Mevcut" : "Yok");

            if (jwt != null) {
                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("doFilterInternal - JWT geçerli. Kullanıcı adı: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("doFilterInternal - UserDetails yüklendi: {}", userDetails.getUsername());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("doFilterInternal - Kullanıcı '{}' kimlik doğrulandı ve SecurityContextHolder ayarlandı.", username);
                } else {
                    logger.warn("doFilterInternal - JWT token geçersiz veya süresi dolmuş. İstek URI: {}", request.getRequestURI());
                }
            } else {
                logger.debug("doFilterInternal - İstek başlığında JWT token bulunamadı. İstek URI: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.error("Kullanıcı kimlik doğrulaması ayarlanamadı: {}. İstek URI: {}", e.getMessage(), request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * İstek başlığından JWT token'ı ayrıştırır.
     * @param request HTTP isteği.
     * @return Ayrıştırılan JWT token'ı veya null.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String jwt = headerAuth.substring(7);
            logger.debug("parseJwt - 'Authorization' başlığından JWT ayrıştırıldı: {}", jwt);
            return jwt;
        }
        logger.debug("parseJwt - 'Authorization' başlığı bulunamadı veya 'Bearer ' ile başlamıyor.");
        return null;
    }
}
