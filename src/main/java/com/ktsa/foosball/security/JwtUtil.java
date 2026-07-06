package com.ktsa.foosball.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.ktsa.foosball.model.Role;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "ktsasupersecretjwtkeysupersecretjwtkey123"; // 32+ chars
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 10; // 10 hours

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String identifier, Role role,
                                Integer tokenVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(identifier)
                .claim("role", role.name())
                .claim("tokenVersion", tokenVersion)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256) //key and algo
                .compact();
    }

    public Integer extractTokenVersion(String token) {
        return parseClaims(token).get("tokenVersion", Integer.class);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

