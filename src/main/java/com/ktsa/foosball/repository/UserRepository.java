package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    List<Users> findByNameContainingIgnoreCase(String query);

    boolean existsByEmail(String email);
}
