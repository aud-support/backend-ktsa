package com.ktsa.foosball.security;

import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService userDetailsService,
                                   UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            // Don't block — let Spring Security's permitAll() handle public routes
            filterChain.doFilter(request, response);
            return;
        }

        String identifier = jwtUtil.extractUsername(token);
        Integer tokenVersion =
                jwtUtil.extractTokenVersion(token);

        if (identifier != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // ADMIN
            if (identifier.equals("admin@gmail.com") || identifier.equals("EMP0001")) {

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                identifier,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {

                // NORMAL USER
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(identifier);

                Users user = userRepository
                        .findByEmail(identifier)
                        .orElse(null);

                if (user == null ||
                        !tokenVersion.equals(user.getTokenVersion())) {

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}

