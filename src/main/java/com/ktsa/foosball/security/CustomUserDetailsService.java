package com.ktsa.foosball.security;


import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        // 1️⃣ Admin login
        if (identifier.equalsIgnoreCase("admin@gmail.com") ||
                identifier.equalsIgnoreCase("EMP0001")) {

            return org.springframework.security.core.userdetails.User
                    .withUsername("admin@gmail.com")
                    .password("{noop}admin123")   // Keep noop unless moving to bcrypt
                    .roles("ADMIN")
                    .build();
        }

        // 2️⃣ User login by email
        Users user = userRepository.findByEmail(identifier).orElse(null);
        if (user != null) {
            return new CustomUserDetails(user);
        }


        throw new UsernameNotFoundException("User not found with: " + identifier);
    }
}

