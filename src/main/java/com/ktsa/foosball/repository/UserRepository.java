package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    List<Users> findByNameContainingIgnoreCase(String query);

    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    Optional<Users> findByUserName(String userName);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.userName LIKE CONCAT(:base, '%')")
    long countByUserNameStartingWith(@Param("base") String base);
}
