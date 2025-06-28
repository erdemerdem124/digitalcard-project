package com.soliner.digitalcard.webApi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Entegrasyon testleri için özel Spring Security konfigürasyonu.
 * Bu sınıf, test ortamında güvenlik filtre zincirini basitleştirir
 * veya belirli yolları güvenliği devre dışı bırakır.
 * Sadece test bağlamında yüklenir.
 */
@TestConfiguration // Bu sınıfın sadece testlerde kullanılacak bir konfigürasyon olduğunu belirtir
@EnableWebSecurity // Güvenlik yapılandırmasını etkinleştirir (sadece test bağlamında)
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF korumasını devre dışı bırak
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() // Tüm isteklere İZİN VER (testler için)
            );
        return http.build();
    }
}
