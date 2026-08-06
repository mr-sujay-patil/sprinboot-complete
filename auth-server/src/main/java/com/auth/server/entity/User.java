package com.auth.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Entity representing a record in the 'users' table.
 */
@Entity
@Table(name = "users") // WHY: 'user' is often a reserved keyword in databases, so we use 'users'.
@Data // WHY: Generates getters, setters, toString, equals, and hashCode.
@NoArgsConstructor // WHY: JPA requires a no-args constructor to instantiate entities via reflection.
@AllArgsConstructor // WHY: Needed for the Builder pattern.
@Builder // WHY: Provides a flexible way to construct User objects without monstrous constructors.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // WHY: IDENTITY strategy delegates ID generation to the database (auto-increment). 
    // It is highly efficient for H2/MySQL as it doesn't require a separate sequence table.
    private Long id;

    @Column(unique = true, nullable = false)
    // WHY: Usernames must be unique to identify users, and cannot be null.
    private String username;

    @Column(nullable = false)
    // WHY: Stores BCrypt hash of the password, NEVER plaintext, to protect user accounts if DB is compromised.
    private String password;

    @Column(nullable = false)
    @Builder.Default
    // WHY: Stores roles as a simple comma-separated string (e.g., "ROLE_USER,ROLE_ADMIN") for simplicity in learning.
    // In production, you would use a separate Role entity and a @ManyToMany relationship.
    private String roles = "ROLE_USER";

}
