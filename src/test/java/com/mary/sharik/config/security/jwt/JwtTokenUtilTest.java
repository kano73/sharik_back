package com.mary.sharik.config.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mary.sharik.exception.NoDataFoundException;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.repository.MyUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

class JwtTokenUtilTest {

    private JwtEncoder encoder;
    private JwtDecoder decoder;
    private MyUserRepository userRepository;
    private JwtTokenUtil jwtTokenUtil;

    private final String testUserId = "user123";
    private MyUser mockUser;

    @BeforeEach
    void setup() throws IllegalAccessException, NoSuchFieldException {
        mockUser = new MyUser();
        mockUser.setId(testUserId);

        encoder = mock(JwtEncoder.class);
        decoder = mock(JwtDecoder.class);
        userRepository = mock(MyUserRepository.class);

        jwtTokenUtil = new JwtTokenUtil(encoder, decoder, userRepository);

        Field accessTokenExpiryField = JwtTokenUtil.class.getDeclaredField("accessTokenExpiry");
        accessTokenExpiryField.setAccessible(true);
        accessTokenExpiryField.set(jwtTokenUtil, Duration.ofHours(1));

        Field refreshTokenExpiryField = JwtTokenUtil.class.getDeclaredField("refreshTokenExpiry");
        refreshTokenExpiryField.setAccessible(true);
        refreshTokenExpiryField.set(jwtTokenUtil, Duration.ofDays(7));

        Field issuerField = JwtTokenUtil.class.getDeclaredField("issuer");
        issuerField.setAccessible(true);
        issuerField.set(jwtTokenUtil, "example.com");
    }

    @Test
    void generateAccessToken_success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("access-token");
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String token = jwtTokenUtil.generateAccessToken(testUserId);
        assertEquals("access-token", token);
    }

    @Test
    void generateAccessToken_userNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        assertThrows(NoDataFoundException.class, () -> jwtTokenUtil.generateAccessToken(testUserId));
    }

    @Test
    void generateRefreshToken_success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockUser));
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("refresh-token");
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String token = jwtTokenUtil.generateRefreshToken(testUserId);
        assertEquals("refresh-token", token);
    }

    @Test
    void generateRefreshToken_userNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        assertThrows(NoDataFoundException.class, () -> jwtTokenUtil.generateRefreshToken(testUserId));
    }

    @Test
    void isTokenNotValid_expiredToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getExpiresAt()).thenReturn(Instant.now().minusSeconds(10));
        when(decoder.decode("token")).thenReturn(jwt);

        boolean result = jwtTokenUtil.isTokenNotValid("token");
        assertTrue(result);
    }

    @Test
    void isTokenNotValid_notExpiredToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
        when(decoder.decode("token")).thenReturn(jwt);

        boolean result = jwtTokenUtil.isTokenNotValid("token");
        assertFalse(result);
    }

    @Test
    void isTokenNotValid_exceptionDuringDecoding() {
        when(decoder.decode("token")).thenThrow(new RuntimeException("decode error"));

        boolean result = jwtTokenUtil.isTokenNotValid("token");
        assertFalse(result);
    }

    @Test
    void getUserIdFromToken_success() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn("user123");
        when(decoder.decode("token")).thenReturn(jwt);

        String userId = jwtTokenUtil.getUserIdFromToken("token");
        assertEquals("user123", userId);
    }

    @Test
    void getUserIdFromToken_missingUserId() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(null);
        when(decoder.decode("token")).thenReturn(jwt);

        JwtException ex = assertThrows(JwtException.class,
                () -> jwtTokenUtil.getUserIdFromToken("token"));
        assertEquals("no userId found", ex.getMessage());
    }

    @Test
    void getUserIdFromToken_decodingException() {
        when(decoder.decode("token")).thenThrow(new RuntimeException("bad token"));

        JwtException ex = assertThrows(JwtException.class,
                () -> jwtTokenUtil.getUserIdFromToken("token"));
        assertTrue(ex.getMessage().contains("exception while parsing token"));
    }
}
