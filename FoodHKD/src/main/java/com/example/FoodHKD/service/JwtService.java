package com.example.FoodHKD.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // ----- Extract username -----
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ----- Check token validity -----
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ----- Check expiration -----
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration == null || expiration.before(new Date());
    }

    // ----- Extract any claim safely -----
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = parseToken(token);
            return claimsResolver.apply(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    // ----- Generate token (1 day expiration) -----
    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities()
                                        .stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .toList();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .addClaims(Map.of("roles", roles)) // gửi role dạng list
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    // ----- Generate refresh token (7 days expiration) -----
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    // ----- Parse token safely -----
    private Claims parseToken(String token) throws JwtException {
        Jws<Claims> jws = Jwts.parserBuilder()
                                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                                .build()
                                .parseClaimsJws(token);
        return jws.getBody();
    }
}
