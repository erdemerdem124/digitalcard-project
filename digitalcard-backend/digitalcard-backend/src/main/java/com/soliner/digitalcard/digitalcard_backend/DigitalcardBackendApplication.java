package com.soliner.digitalcard.digitalcard_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    // Projenizin tüm kök paketlerini buraya ekliyoruz.
    // Varsayılan olarak @SpringBootApplication kendi paketi ve alt paketlerini tarar.
    // Ancak emin olmak ve varsa farklı modülleri/paketleri dahil etmek için açıkça belirtiyoruz.
    "com.soliner.digitalcard" // Projenin ana kök paketi, tüm alt paketleri kapsar.
    // Eğer 'com.soliner.digitalcard' dışında kalan ve Spring bean'i içeren paketleriniz varsa,
    // örneğin 'com.external.library.xyz' gibi, onları da buraya eklemelisiniz.
})
@EntityScan(basePackages = {
    // Tüm JPA @Entity sınıflarınızın bulunduğu paketleri buraya ekliyoruz.
    // Genellikle 'domain' veya 'model' katmanında olurlar.
    "com.soliner.digitalcard.domain.user",
    "com.soliner.digitalcard.domain.sociallink",
    "com.soliner.digitalcard.domain.project",
    "com.soliner.digitalcard.domain" // Eğer bazı entity'ler doğrudan domain paketindeyse
    // Ek entity paketleriniz varsa ekleyin: "com.soliner.digitalcard.domain.otherEntity"
})
@EnableJpaRepositories(basePackages = {
    // Tüm JPA @Repository interface'lerinizin bulunduğu paketleri buraya ekliyoruz.
    // Genellikle 'infrastructure.repository' veya 'dataAccess' katmanında olurlar.
    "com.soliner.digitalcard.persistence.repository",
    // Ek repository paketleriniz varsa ekleyin: "com.soliner.digitalcard.infrastructure.otherRepository"
})
public class DigitalcardBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalcardBackendApplication.class, args);
    }
}