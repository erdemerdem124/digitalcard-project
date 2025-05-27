package com.soliner.digitalcard.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // Enum tipindeki rolleri String olarak sakla
    @Column(length = 20)
    private ERole name; // ERole enum'ı kullanılacak

    // Constructor'lar, getter/setter'lar Lombok @Data ile otomatik oluşur
}