package com.mary.sharik.service;

import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exception.CredentialsNotUniqueExceptions;
import com.mary.sharik.exception.NoDataFoundException;
import com.mary.sharik.model.dto.request.MyUserRegisterDTO;
import com.mary.sharik.model.dto.request.MyUserSearchFilterDTO;
import com.mary.sharik.model.dto.request.MyUserUpdateDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.Role;
import com.mary.sharik.repository.MyUserRepository;
import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserServiceTest{

    @Mock
    private MyUserRepository myUserRepository;

    @Mock
    private MyUserValidationService myUserValidationService;

    @Mock
    private AuthenticatedMyUserService authenticatedMyUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MyUserService myUserService;

    MyUser myUser;

    @BeforeEach
    void setUp() {
        myUser = new MyUser();
        myUser.setFirstName("Mary");
        myUser.setLastName("Smith");
        myUser.setEmail("mary.smith@gmail.com");
        myUser.setPassword("password");
        myUser.setAddress("street: address, house number: 10");

        ReflectionTestUtils.setField(myUserService, "PAGE_SIZE", 10);
    }

    @Test
    void register_success() {
        doNothing().when(myUserValidationService).credentialsUniqueOrThrow(any(MyUser.class));

        MyUserRegisterDTO myUserRegisterDTO = new MyUserRegisterDTO();
        myUserRegisterDTO.setFirstName("Mary");
        myUserRegisterDTO.setLastName("Smith");
        myUserRegisterDTO.setEmail("mary.smith@gmail.com");
        myUserRegisterDTO.setPassword("password");
        myUserRegisterDTO.setAddress("street: address, house number: 10");

        myUserService.register(myUserRegisterDTO);

        verify(myUserRepository, times(1)).save(argThat(user ->
                user.getRole() == Role.USER
        ));
    }

    @Test
    void register_unUniqueEmail_throwsException() {
        doThrow(new CredentialsNotUniqueExceptions("email already in use")).when(myUserValidationService).credentialsUniqueOrThrow(any(MyUser.class));

        MyUserRegisterDTO myUserRegisterDTO = new MyUserRegisterDTO();
        myUserRegisterDTO.setFirstName("Mary");
        myUserRegisterDTO.setLastName("Smith");
        myUserRegisterDTO.setEmail("mary.smith@gmail.com");
        myUserRegisterDTO.setPassword("password");
        myUserRegisterDTO.setAddress("street: address, house number: 10");

        assertThrows(CredentialsNotUniqueExceptions.class,
                () -> myUserService.register(myUserRegisterDTO));
    }

    @Test
    void findByEmail_existingUser_returnsUser() {
        when(myUserRepository.findByEmailEqualsIgnoreCase("mary.smith@gmail.com"))
                .thenReturn(Optional.of(myUser));

        MyUser found = myUserService.findByEmail("mary.smith@gmail.com");

        assertEquals(myUser, found);
    }

    @Test
    void findByEmail_notFound_throwsException() {
        when(myUserRepository.findByEmailEqualsIgnoreCase("notfound@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                myUserService.findByEmail("notfound@mail.com"));
    }


    @Test
    void getUserInfo_success() {
        myUser.setId("123");
        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(myUser);
        when(myUserRepository.findById("123")).thenReturn(Optional.of(myUser));

        MyUserPublicInfoDTO dto = myUserService.getUserInfo();

        assertEquals(myUser.getEmail(), dto.email());
    }

    @Test
    void updateInfo_success() {
        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(myUser);
        when(myUserRepository.save(any())).thenReturn(myUser);

        MyUserUpdateDTO dto = new MyUserUpdateDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@mail.com");
        dto.setAddress("New address");

        MyUserPublicInfoDTO updated = myUserService.updateInfo(dto);

        assertEquals("john.doe@mail.com", updated.email());
        verify(myUserRepository).save(argThat(user ->
                user.getEmail().equals("john.doe@mail.com")
        ));
    }
    @Test
    void getUsersByFilters_success() {
        MyUserSearchFilterDTO filter = new MyUserSearchFilterDTO();
        filter.setEmail("mary@mail.com");
        filter.setPage(1);

        when(myUserRepository.findByFilters(
                any(), any(), any(), any(), any()
        )).thenReturn(List.of(myUser));

        List<MyUserPublicInfoDTO> result = myUserService.getUsersByFilters(filter);

        assertEquals(1, result.size());
        assertEquals(myUser.getEmail(), result.getFirst().email());
    }

    @Test
    void isUserAdmin_true() {
        myUser.setRole(Role.ADMIN);
        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(myUser);

        assertTrue(myUserService.isUserAdmin());
    }

    @Test
    void isUserAdmin_false() {
        myUser.setRole(Role.USER);
        when(authenticatedMyUserService.getCurrentUserAuthenticated()).thenReturn(myUser);

        assertFalse(myUserService.isUserAdmin());
    }

    @Test
    void isUserAdmin_exception_returnsFalse() {
        when(authenticatedMyUserService.getCurrentUserAuthenticated())
                .thenThrow(new NoDataFoundException("not found"));

        assertFalse(myUserService.isUserAdmin());
    }

}