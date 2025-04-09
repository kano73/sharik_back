package com.mary.sharik.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.mary.sharik.config.security.jwt.JwtTokenUtil;
import com.mary.sharik.exception.ValidationFailedException;
import com.mary.sharik.model.dto.request.AuthRequest;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private MyUserService myUserService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private GoogleIdTokenVerifier verifier;

    @InjectMocks
    private AuthService authService;

    private final String TEST_USER_ID = "test-user-id";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "password";
    private final String TEST_ACCESS_TOKEN = "access_token";
    private final String TEST_REFRESH_TOKEN = "refresh_token";
    private final String TEST_GOOGLE_TOKEN = "google_token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expirationTimeAccess", Duration.ofMinutes(30));
        ReflectionTestUtils.setField(authService, "expirationTimeRefresh", Duration.ofDays(7));
        ReflectionTestUtils.setField(authService, "GOOGLE_CLIENT_ID", "test-client-id");
        ReflectionTestUtils.setField(authService, "verifier", verifier);
    }

    @Test
    void init_shouldCreateGoogleIdTokenVerifier() {
        authService.init();
        assertNotNull(authService.getVerifier());
    }

    @Test
    void loginWithGoogleIdToken_Success() throws GeneralSecurityException, IOException {
        // Arrange
        GoogleIdToken mockToken = Mockito.mock(GoogleIdToken.class);
        Payload mockPayload = Mockito.mock(Payload.class);
        MyUser mockUser = Mockito.mock(MyUser.class);

        when(verifier.verify(TEST_GOOGLE_TOKEN)).thenReturn(mockToken);
        when(mockToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn(TEST_EMAIL);
        when(myUserService.findByEmail(TEST_EMAIL)).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(TEST_USER_ID);
        when(jwtTokenUtil.generateAccessToken(TEST_USER_ID)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenUtil.generateRefreshToken(TEST_USER_ID)).thenReturn(TEST_REFRESH_TOKEN);

        // Act
        ResponseEntity<?> response = authService.loginWithGoogleIdToken(TEST_GOOGLE_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody());
        verify(verifier).verify(TEST_GOOGLE_TOKEN);
        verify(myUserService).findByEmail(TEST_EMAIL);
        verify(jwtTokenUtil).generateAccessToken(TEST_USER_ID);
        verify(jwtTokenUtil).generateRefreshToken(TEST_USER_ID);
    }

    @Test
    void loginWithGoogleIdToken_InvalidToken() throws GeneralSecurityException, IOException {
        // Arrange
        when(verifier.verify(TEST_GOOGLE_TOKEN)).thenReturn(null);

        // Act
        ResponseEntity<?> response = authService.loginWithGoogleIdToken(TEST_GOOGLE_TOKEN);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Please register", response.getBody());
    }

    @Test
    void loginWithGoogleIdToken_VerificationException() throws GeneralSecurityException, IOException {
        // Arrange
        when(verifier.verify(TEST_GOOGLE_TOKEN)).thenThrow(new GeneralSecurityException());

        // Act
        ResponseEntity<?> response = authService.loginWithGoogleIdToken(TEST_GOOGLE_TOKEN);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Please register", response.getBody());
    }

    @Test
    void loginProcess_Success() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        MyUser mockUser = Mockito.mock(MyUser.class);

        when(myUserService.findByEmail(TEST_EMAIL)).thenReturn(mockUser);
        when(passwordEncoder.matches(TEST_PASSWORD, mockUser.getPassword())).thenReturn(true);
        when(mockUser.getId()).thenReturn(TEST_USER_ID);
        when(jwtTokenUtil.generateAccessToken(TEST_USER_ID)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenUtil.generateRefreshToken(TEST_USER_ID)).thenReturn(TEST_REFRESH_TOKEN);

        // Act
        ResponseEntity<?> response = authService.loginProcess(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        verify(myUserService).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, mockUser.getPassword());
        verify(jwtTokenUtil).generateAccessToken(TEST_USER_ID);
        verify(jwtTokenUtil).generateRefreshToken(TEST_USER_ID);
    }

    @Test
    void loginProcess_UserNotFound() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        when(myUserService.findByEmail(TEST_EMAIL)).thenReturn(null);

        // Act
        ResponseEntity<?> response = authService.loginProcess(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
        verify(myUserService).findByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void loginProcess_IncorrectPassword() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        MyUser mockUser = Mockito.mock(MyUser.class);
        String TEST_ENCODED_PASSWORD = "encoded_password";
        when(mockUser.getPassword()).thenReturn(TEST_ENCODED_PASSWORD);

        when(myUserService.findByEmail(TEST_EMAIL)).thenReturn(mockUser);
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(false);

        // Act
        ResponseEntity<?> response = authService.loginProcess(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
        verify(myUserService).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD);
    }

    @Test
    void generateTokensWithId_Success() {
        // Arrange
        when(jwtTokenUtil.generateAccessToken(TEST_USER_ID)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenUtil.generateRefreshToken(TEST_USER_ID)).thenReturn(TEST_REFRESH_TOKEN);

        // Act
        ResponseEntity<?> response = authService.generateTokensWithId(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        verify(jwtTokenUtil).generateAccessToken(TEST_USER_ID);
        verify(jwtTokenUtil).generateRefreshToken(TEST_USER_ID);
    }

    @Test
    void tokenToCookie_AccessToken() {
        // Act
        ResponseCookie cookie = authService.tokenToCookie(TEST_ACCESS_TOKEN, TokenType.accessToken);

        // Assert
        assertEquals(TokenType.accessToken.toString(), cookie.getName());
        assertEquals(TEST_ACCESS_TOKEN, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertEquals(authService.getExpirationTimeAccess().getSeconds(), cookie.getMaxAge().getSeconds());
        assertEquals("Strict", cookie.getSameSite());
    }

    @Test
    void tokenToCookie_RefreshToken() {
        // Act
        ResponseCookie cookie = authService.tokenToCookie(TEST_REFRESH_TOKEN, TokenType.refreshToken);

        // Assert
        assertEquals(TokenType.refreshToken.toString(), cookie.getName());
        assertEquals(TEST_REFRESH_TOKEN, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertEquals(authService.getExpirationTimeRefresh().getSeconds(), cookie.getMaxAge().getSeconds());
        assertEquals("Strict", cookie.getSameSite());
    }

    @Test
    void tokenToCookie_UnknownType() {
        // Act & Assert
        assertThrows(ValidationFailedException.class, () -> authService.tokenToCookie(TEST_ACCESS_TOKEN, null));
    }

    @Test
    void logout_ShouldReturnEmptyCookies() {
        // Act
        ResponseEntity<?> response = authService.logout();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out", response.getBody());

        HttpHeaders headers = response.getHeaders();
        assertTrue(headers.containsKey(HttpHeaders.SET_COOKIE));

        // Verify that there are two cookies (access and refresh)
        assertEquals(2, Objects.requireNonNull(headers.get(HttpHeaders.SET_COOKIE)).size());

        // Check that both cookies have empty values
        Objects.requireNonNull(headers.get(HttpHeaders.SET_COOKIE)).forEach(cookie ->
                assertTrue(cookie.contains("=;") || cookie.contains("=\"\";")));
    }
}