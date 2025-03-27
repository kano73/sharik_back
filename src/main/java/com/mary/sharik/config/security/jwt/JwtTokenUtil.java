package com.mary.sharik.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${max.age.access}")
    private long expirationTimeAccess;

    @Value("${max.age.refresh}")
    private long expirationTimeRefresh;

    public String generateAccessToken(String id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("token_type", "access");
        return buildToken(claims, expirationTimeAccess);
    }

    public String generateRefreshToken(String id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("token_type", "refresh");
        return buildToken(claims, expirationTimeRefresh);
    }

    private String buildToken(Map<String, Object> claims, long expirationTimeRefresh) {
        claims.put("iat", new Date());

        claims.put("exp", new Date(System.currentTimeMillis() + expirationTimeRefresh));

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        JwtBuilder builder = Jwts.builder().signWith(key);
        claims.forEach(builder::claim);

        return builder.compact();
    }

    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token).getPayload();

            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token).getPayload();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
