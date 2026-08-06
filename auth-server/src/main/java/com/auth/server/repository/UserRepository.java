package com.auth.server.repository;

import com.auth.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * 
 * WHY: Extending JpaRepository provides standard CRUD operations (save, findById, delete, etc.) 
 * without writing any implementation code. Spring Data JPA generates a proxy at runtime.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // WHY: Spring Data's derived query mechanism parses the method name 'findByUsername' 
    // and generates the corresponding SQL query (SELECT * FROM users WHERE username = ?).
    // Optional is used to cleanly handle cases where the user is not found.
    Optional<User> findByUsername(String username);

    // WHY: Useful during registration to quickly check if a username is already taken 
    // without loading the entire User entity from the database.
    Boolean existsByUsername(String username);

}
