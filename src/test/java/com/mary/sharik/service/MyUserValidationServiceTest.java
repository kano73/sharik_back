package com.mary.sharik.service;

import com.mary.sharik.exception.CredentialsNotUniqueExceptions;
import com.mary.sharik.exception.NoDataFoundException;
import com.mary.sharik.exception.ValidationFailedException;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.repository.MyUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserValidationServiceTest {

    @InjectMocks
    private MyUserValidationService myUserValidationService;

    @Mock
    private MyUserRepository myUserRepository;

    @Mock
    private OAuth2User oAuth2User;

    MyUser myUser;

    @BeforeEach
    void setUp() {
        myUser = new MyUser();
        myUser.setFirstName("Mary");
        myUser.setLastName("Smith");
        myUser.setEmail("mary.smith@gmail.com");
        myUser.setPassword("password");
    }

    @Test
    void credentialsUniqueOrThrow_throwsException() {
        when(myUserRepository.existsByEmailEqualsIgnoreCase(any(String.class))).thenReturn(false);
        assertDoesNotThrow(()-> myUserValidationService.credentialsUniqueOrThrow(myUser));
    }

    @Test
    void credentialsUniqueOrThrow_success() {
        when(myUserRepository.existsByEmailEqualsIgnoreCase(any(String.class))).thenReturn(true);
        assertThrows(CredentialsNotUniqueExceptions.class, ()->
                myUserValidationService.credentialsUniqueOrThrow(myUser));
    }


    @Test
    void isEmailValid_validEmail_userExists() {


        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", myUser.getEmail()));
        when(myUserRepository.findByEmailEqualsIgnoreCase(myUser.getEmail())).thenReturn(Optional.of(myUser));

        assertDoesNotThrow(() -> myUserValidationService.isEmailValid(oAuth2User));
    }

    @Test
    void isEmailValid_nullEmail_throwsValidationFailedException() {
        when(oAuth2User.getAttributes()).thenReturn(Map.of());

        assertThrows(ValidationFailedException.class, () -> myUserValidationService.isEmailValid(oAuth2User));
    }

    @Test
    void isEmailValid_userNotFound_throwsNoDataFoundException() {
        String email = "missing@mail.com";

        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", email));
        when(myUserRepository.findByEmailEqualsIgnoreCase(email)).thenReturn(Optional.empty());

        assertThrows(NoDataFoundException.class, () -> myUserValidationService.isEmailValid(oAuth2User));
    }
}