package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
}
