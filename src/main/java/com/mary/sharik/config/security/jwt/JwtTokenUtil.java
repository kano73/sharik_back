package com.mary.sharik.config.security.jwt;

import com.mary.sharik.model.enums.RoleEnum;
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

    public String generateAccessToken(String id, RoleEnum role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role.name());
        claims.put("iat", new Date());
        // 10 hour
        long expirationTime = 36_000_000;
        claims.put("exp", new Date(System.currentTimeMillis() + expirationTime));

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        JwtBuilder builder = Jwts.builder()
                .signWith(key);

        claims.forEach(builder::claim);

        return builder.compact();
    }

    public String generateRefreshToken(String id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("token_type", "refresh");
        claims.put("iat", new Date());
        // 7 days
        long expirationTime = 605_000_000;
        claims.put("exp", new Date(System.currentTimeMillis() + expirationTime));

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        JwtBuilder builder = Jwts.builder().signWith(key);
        claims.forEach(builder::claim);

        return builder.compact();
    }

    public boolean isTokenInvalid(String token) {
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
