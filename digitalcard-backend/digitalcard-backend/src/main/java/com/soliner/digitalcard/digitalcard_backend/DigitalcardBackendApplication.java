package com.soliner.digitalcard.digitalcard_backend;

import com.soliner.digitalcard.domain.model.ERole; // ERole sınıfı import edildi
import com.soliner.digitalcard.domain.model.Role;    // Role sınıfı import edildi
import com.soliner.digitalcard.persistence.repository.RoleRepository; // RoleRepository import edildi
import org.springframework.boot.CommandLineRunner; // CommandLineRunner import edildi
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;      // Bean anotasyonu import edildi
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile; // Profile anotasyonu import edildi
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot uygulamasının ana başlangıç sınıfı.
 * Uygulamanın çalışması için gerekli tüm Spring Boot yapılandırmalarını içerir.
 * Ayrıca uygulama başlangıcında veritabanına varsayılan rolleri ekler (yalnızca 'dev' profili için, 'test' hariç).
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    // Projenizin tüm kök paketlerini buraya ekliyoruz.
    // Varsayılan olarak @SpringBootApplication kendi paketi ve alt paketlerini tarar.
    // Ancak emin olmak ve varsa farklı modülleri/paketleri dahil etmek için açıkça belirtiyoruz.
    "com.soliner.digitalcard" // Projenin ana kök paketi, tüm alt paketleri kapsar.
})
@EntityScan(basePackages = {
    // Tüm JPA @Entity sınıflarınızın bulunduğu paketleri buraya ekliyoruz.
    // Genellikle 'domain' veya 'model' katmanında olurlar.
    "com.soliner.digitalcard.domain.model" // Entity'ler genellikle domain.model paketinde olur
})
@EnableJpaRepositories(basePackages = {
    // Tüm JPA @Repository interface'lerinizin bulunduğu paketleri buraya ekliyoruz.
    // Genellikle 'persistence.repository' katmanında olurlar.
    "com.soliner.digitalcard.persistence.repository"
})
public class DigitalcardBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalcardBackendApplication.class, args);
    }

    /**
     * Uygulama başlangıcında (yalnızca 'test' profili aktif değilken) varsayılan rolleri veritabanına ekler.
     * Bu, uygulamanın çalışması için gerekli temel rollerin mevcut olmasını sağlar.
     * Bu metod, sadece veritabanı boşken veya roller eksikken çalıştırılmalıdır
     * ve üretim ortamında dikkatli kullanılmalıdır.
     *
     * @param roleRepository Rol veritabanı işlemleri için repository
     * @return CommandLineRunner nesnesi
     */
    @Bean
    @Profile("!test") // KRİTİK DÜZELTME: Sadece 'test' profili aktif DEĞİLKEN çalışır
    public CommandLineRunner createDefaultRoles(RoleRepository roleRepository) {
        return args -> {
            // ROLE_USER rolünü kontrol et ve yoksa ekle
            if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
                Role userRole = Role.builder().name(ERole.ROLE_USER).build();
                roleRepository.save(userRole);
                System.out.println("ROLE_USER başarıyla eklendi.");
            }

            // ROLE_ADMIN rolünü kontrol et ve yoksa ekle (opsiyonel, uygulamanızda admin rolü varsa)
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                Role adminRole = Role.builder().name(ERole.ROLE_ADMIN).build();
                roleRepository.save(adminRole);
                System.out.println("ROLE_ADMIN başarıyla eklendi.");
            }
        };
    }
}
