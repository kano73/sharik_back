package com.mary.sharik.service;

import com.mary.sharik.containerConfig.AbstractPostgresContainerTest;
import com.mary.sharik.model.details.MyUserDetails;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.repository.MyUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MyUserDetailsServiceTest extends AbstractPostgresContainerTest {

    @Autowired
    private MyUserRepository userRepository;

    private MyUserDetailsService myUserDetailsService;

    private MyUser myUser;

    @BeforeEach
    void setUp() {
        myUserDetailsService = new MyUserDetailsService(userRepository);

        myUser = new MyUser();
        myUser.setFirstName("Mary");
        myUser.setLastName("Smith");
        myUser.setEmail("mary.smith@gmail.com");
        myUser.setPassword("password");
        myUser.setAddress("street: address, house number: 10");
        userRepository.save(myUser);
    }

    @Test
    void loadUserByUsername_success() {
        MyUserDetails userDetails = (MyUserDetails) myUserDetailsService.loadUserByUsername("marY.smiTh@gmail.com");
        MyUser user = userDetails.myUser();
        assertEquals(myUser.getEmail(), user.getEmail());
    }

    @Test
    void loadUserByUsername_throwsException() {
        assertThrows(UsernameNotFoundException.class,
                () -> myUserDetailsService.loadUserByUsername("marY.smiTh1@gmail.com"));
    }
}