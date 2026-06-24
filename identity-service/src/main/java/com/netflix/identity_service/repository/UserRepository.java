package com.netflix.identity_service.repository;

import com.netflix.identity_service.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // This tells Hibernate: "I need this user AND their roles in one trip"
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
