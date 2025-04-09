package com.mary.sharik.config;

import com.mary.sharik.containerConfig.AbstractPostgresContainerTest;
import com.mary.sharik.repository.MyUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@Import({DataInitializer.class, PasswordConfiguration.class})
class DataInitializerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MyUserRepository userRepository;

    @Test
    void run() {
        long count = userRepository.count();
        Assertions.assertTrue(count != 0 , "Expected not zero number of users to be initialized");
    }
}