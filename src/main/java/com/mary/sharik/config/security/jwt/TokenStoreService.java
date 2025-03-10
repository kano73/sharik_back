package com.mary.sharik.config.security.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenStoreService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public TokenStoreService(RedisTemplate<String, String> redisTemplate, JwtTokenUtil jwtTokenUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public void storeAccessToken(String token, String userId) {
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        if (claims != null) {
            // Store token with user ID as key
            redisTemplate.opsForValue().set(
                    "TOKEN_" + userId,
                    token,
                    claims.getExpiration().getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public void storeRefreshToken(String token, String userId) {
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        if (claims != null) {
            redisTemplate.opsForValue().set(
                    "REFRESH_TOKEN_" + userId,
                    token,
                    claims.getExpiration().getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public void invalidateToken(String token) {
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        if (claims != null) {
            // Store in blacklist until expiration
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set("BL_" + token, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void invalidateAllUserTokens(String userId) {
        String token = redisTemplate.opsForValue().get("TOKEN_" + userId);
        if (token != null) {
            invalidateToken(token);
            redisTemplate.delete("TOKEN_" + userId);
        }
    }
}