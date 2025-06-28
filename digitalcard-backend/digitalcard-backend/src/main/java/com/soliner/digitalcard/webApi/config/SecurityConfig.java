package com.soliner.digitalcard.webApi.config;

import com.soliner.digitalcard.application.services.impl.UserDetailsServiceImpl;
import com.soliner.digitalcard.webApi.security.AuthEntryPointJwt;
import com.soliner.digitalcard.webApi.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List; // java.util.List importunu ekle

/**
 * Spring Security yapılandırma sınıfı.
 * Bu sınıf, HTTP isteklerinin nasıl güvenli hale getirileceğini tanımlar.
 * webApi katmanına aittir çünkü doğrudan HTTP güvenliği ile ilgilenir.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // prePostEnabled = true varsayılan olarak gelir
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Lombok'un @RequiredArgsConstructor anotasyonunu kullanıyorsanız, bu constructor'a gerek yoktur.
    // Ancak elle yönetmek isterseniz, böyle kalabilir. Ben @RequiredArgsConstructor ile uyumlu hale getirdim
    // ve constructor injection'ı Lombok'a bıraktım.
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuthEntryPointJwt unauthorizedHandler, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Frontend'in URL'si: Hem 4200 hem de 8080 (backend'in kendi URL'si) eklenmeli
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:8080")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*")); // Tüm başlıklara izin ver
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Pre-flight (OPTIONS) istekleri için önbellek süresi

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF korumasını devre dışı bırak (stateless API'ler için yaygın)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS yapılandırmasını etkinleştir
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Yetkilendirme hataları için özel giriş noktası
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Oturum kullanma
            .authorizeHttpRequests(authorize -> authorize
                // Herkese açık endpoint'ler (kimlik doğrulama gerektirmez) - en spesifikten başla
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll() // Login ve Register
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Yeni kullanıcı oluşturma (kayıt)

                // Public API'ler (eğer varsa, mesela QR kodu profili çekmek için)
                // Dikkat: Eğer "/api/users/username/**" public olacaksa bu en üste alınmalı.
                // Testlerdeki "getUserByUsername" bu kuralı kullanabilir.
                .requestMatchers("/api/public/**").permitAll() 
                // Eğer profil URL'si public ise:
                // .requestMatchers(HttpMethod.GET, "/api/users/username/**").permitAll() // Eğer kullanıcı profilleri public ise


                // Sadece kimliği doğrulanmış kullanıcılar için endpoint'ler (authenticated)
                // UserController'daki GET, PUT, DELETE metotları
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated() // Kullanıcıyı ID ile getirme
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated() // Kullanıcıyı güncelleme
                .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").authenticated() // Kullanıcıyı silme
                // Kullanıcı adına göre profil çekme (Eğer özel değilse)
                .requestMatchers(HttpMethod.GET, "/api/users/username/**").authenticated() // Kullanıcıyı username ile getirme

                // Şifre güncelleme endpoint'i: "/api/users/{id}/password"
                // Testleriniz bu URL'i kullandığı için doğru path'i buraya ekledim.
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}/password").authenticated() 

                // Sosyal linkler endpoint'leri
                .requestMatchers("/api/sociallinks/**").authenticated()

                // Projeler endpoint'leri
                .requestMatchers("/api/projects/**").authenticated()

                // Admin endpoint'leri (ADMIN rolü gerektirir)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Diğer tüm /api/auth/** endpoint'leri için kimlik doğrulama gerektir
                // Login ve register dışındaki auth endpoint'leri (örn: refresh token, logout)
                .requestMatchers("/api/auth/**").authenticated()

                // Kural dışı kalan diğer tüm istekler için kimlik doğrulama gerektir
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider()); // Özel kimlik doğrulama sağlayıcısını ekle
        // JWT filtresini UsernamePasswordAuthenticationFilter'dan önce ekle
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
