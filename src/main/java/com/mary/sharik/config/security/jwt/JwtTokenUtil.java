package com.mary.sharik.config.security.jwt;

import com.mary.sharik.exception.NoDataFoundException;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.TokenType;
import com.mary.sharik.repository.MyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class JwtTokenUtil {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final MyUserRepository myUserRepository;

    @Value("${jwt.access-token.expiry:PT1H}")
    private Duration accessTokenExpiry;

    @Value("${jwt.refresh-token.expiry:P7D}")
    private Duration refreshTokenExpiry;

    @Value("${jwt.issuer:example.com}")
    private String issuer;

    public String generateAccessToken(String id) {
        Instant now = Instant.now();

        MyUser user = myUserRepository.findById(id).orElseThrow(() ->
                new NoDataFoundException("no user found for id " + id));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpiry))
                .subject(user.getId())
                .claim("userId", user.getId())
                .claim("type", TokenType.accessToken.name())
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(String id) {
        Instant now = Instant.now();

        MyUser user = myUserRepository.findById(id).orElseThrow(() ->
                new NoDataFoundException("no user found for id " + id));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenExpiry))
                .subject(user.getId()).claim("userId", user.getId())
                .claim("type", TokenType.refreshToken.name())
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean isTokenNotValid(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            return Objects.requireNonNull(jwt.getExpiresAt()).isBefore(Instant.now());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) throws JwtException {
        try {
            Jwt jwt = decoder.decode(token);
            Object userId = jwt.getClaim("userId");

            if (userId == null) {
                throw new JwtException("no userId found");
            }

            return (String) userId;
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("exception while parsing token: " + e.getMessage(), e);
        }
    }
}
