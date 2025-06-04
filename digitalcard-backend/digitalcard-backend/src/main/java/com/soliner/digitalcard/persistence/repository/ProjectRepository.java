package com.soliner.digitalcard.persistence.repository;


import com.soliner.digitalcard.domain.model.Project; // Project Entity'sini import ediyoruz
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA'nın temel Repository arayüzünü import ediyoruz
import org.springframework.stereotype.Repository; // İsteğe bağlı ama iyi uygulama için eklenir

import java.util.List; // findByUserId metodu için

/**
 * Proje (Project) Entity'si için veri erişim işlemlerini sağlayan Repository arayüzü.
 * JpaRepository'den miras alarak temel CRUD operasyonlarını otomatik olarak sağlar.
 * <Project, Long>: İlk parametre Entity tipi, ikinci parametre Entity'nin ID'sinin tipi.
 * persistence katmanına aittir.
 */
@Repository // Bu arayüzün bir Spring bileşeni olduğunu ve veri erişim katmanına ait olduğunu belirtir
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Belirli bir kullanıcıya ait tüm projeleri bulur.
     * Spring Data JPA, metod isminden otomatik olarak sorguyu oluşturur.
     * @param userId Projelerin ait olduğu kullanıcının ID'si.
     * @return Belirtilen kullanıcıya ait Project listesi.
     */
    List<Project> findByUser_Id(Long userId); // <-- Burası düzeltildi!
}

