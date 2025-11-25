package com.example.medigo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-access}")
    private Long accessTokenExpiration;

    @Value("${jwt.expiration-refresh}")
    private Long refreshTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        log.debug("Generating token for user: {} with roles: {}", userDetails.getUsername(), roles);
        
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            
            log.debug("Token validated successfully for user: {}", claims.getPayload().getSubject());
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid or expired
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String username = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            
            log.debug("Username extracted from token: {}", username);
            return username;
        } catch (JwtException e) {
            log.debug("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }
    
    public List<String> extractRoles(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            log.debug("Roles extracted from token: {}", roles);
            return roles;
        } catch (JwtException e) {
            log.debug("Failed to extract roles from token: {}", e.getMessage());
            return null;
        }
    }
}