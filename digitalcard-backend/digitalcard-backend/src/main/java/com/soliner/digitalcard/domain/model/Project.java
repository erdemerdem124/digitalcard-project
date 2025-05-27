package com.soliner.digitalcard.domain.model; 

import jakarta.persistence.*; // JPA annotations
import lombok.AllArgsConstructor; // Lombok: All-args constructor
import lombok.Builder; // Lombok: Builder pattern
import lombok.Data; // Lombok: Getters, Setters, etc.
import lombok.NoArgsConstructor; // Lombok: No-args constructor

/**
 * Project entity representing a user's project.
 * Maps to the 'projects' table in the database.
 * Belongs to the domain layer.
 */
@Entity // Specifies that this class is a JPA entity
@Table(name = "projects") // Specifies the table name in the database
@Data // Lombok: Generates getters, setters, equals, hashCode, and toString methods
@NoArgsConstructor // Lombok: Generates a no-argument constructor
@AllArgsConstructor // Lombok: Generates a constructor with all arguments
@Builder // Lombok: Generates a builder pattern for object creation
public class Project {

    @Id // Specifies the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Specifies auto-increment for the ID
    private Long id;

    @Column(nullable = false, length = 100) // Not null and max length
    private String title; // Project title

    @Column(length = 1000) // Max length
    private String description; // Project description

    @Column(length = 255) // Max length
    private String projectUrl; // Live URL or repository URL of the project

    @Column(length = 255) // Max length
    private String technologies; // Technologies used (e.g., "Spring Boot, React, PostgreSQL")

    // Many-to-One relationship: Many projects can belong to one user
    // optional = false: This project must have an associated user
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading for performance
    @JoinColumn(name = "user_id", nullable = false) // Specifies the foreign key column in the database
    private User user; // Associated User entity
}
