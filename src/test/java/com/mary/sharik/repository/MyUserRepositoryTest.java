package com.mary.sharik.repository;

import com.mary.sharik.containerConfig.AbstractPostgresContainerTest;
import com.mary.sharik.model.dto.request.MyUserSearchFilterDTO;
import com.mary.sharik.model.entity.MyUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MyUserRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private MyUserRepository myUserRepository;

    @BeforeEach
    void setUp() {
        MyUser myUser1 = new MyUser();
        myUser1.setFirstName("John");
        myUser1.setLastName("Doe");
        myUser1.setEmail("john.doe@example.com");
        myUser1.setPassword("password");
        myUser1.setAddress("Test Address 1");

        MyUser myUser2 = new MyUser();
        myUser2.setFirstName("Jane");
        myUser2.setLastName("Doe");
        myUser2.setEmail("jane.doe@example.com");
        myUser2.setPassword("password");
        myUser2.setAddress("Test Address 2");

        MyUser myUser3 = new MyUser();
        myUser3.setFirstName("Alice");
        myUser3.setLastName("Smith");
        myUser3.setEmail("alice.smith@example.com");
        myUser3.setPassword("password");
        myUser3.setAddress("Test Address 3");

        myUserRepository.saveAll(List.of(myUser1, myUser2, myUser3));
    }

    @Test
    void findByFilters_withFirstName_success() {
        // Параметры для фильтрации
        MyUserSearchFilterDTO filter = new MyUserSearchFilterDTO();
        filter.setFirstOrLastName("John");
        filter.setPage(1);

        // Проверяем, что пользователь найден по firstName
        List<MyUser> users = myUserRepository.findByFilters(
                filter.getFirstOrLastName(),
                null,
                null,
                null,
                PageRequest.of(filter.getPage() - 1, 10) // PAGE_SIZE = 10
        );

        assertEquals(1, users.size());
        assertEquals("John", users.getFirst().getFirstName());
    }

    @Test
    void findByFilters_withEmail_success() {
        // Параметры для фильтрации
        MyUserSearchFilterDTO filter = new MyUserSearchFilterDTO();
        filter.setEmail("john.doe@example.com");
        filter.setPage(1);

        // Проверяем, что пользователь найден по email
        List<MyUser> users = myUserRepository.findByFilters(
                null,
                null,
                filter.getEmail(),
                null,
                PageRequest.of(filter.getPage() - 1, 10) // PAGE_SIZE = 10
        );

        assertEquals(1, users.size());
        assertEquals("john.doe@example.com", users.getFirst().getEmail());
    }

    @Test
    void findByFilters_withFirstNameAndEmail_success() {
        // Параметры для фильтрации, ищем по имени и email одновременно
        MyUserSearchFilterDTO filter = new MyUserSearchFilterDTO();
        filter.setFirstOrLastName("John");
        filter.setEmail("john.doe@example.com");
        filter.setPage(1);

        // Проверяем, что пользователь найден по имени и email
        List<MyUser> users = myUserRepository.findByFilters(
                filter.getFirstOrLastName(),
                null,
                filter.getEmail(),
                null,
                PageRequest.of(filter.getPage() - 1, 10) // PAGE_SIZE = 10
        );

        assertEquals(1, users.size());
        assertEquals("John", users.getFirst().getFirstName());
        assertEquals("john.doe@example.com", users.getFirst().getEmail());
    }

    @Test
    void findByFilters_withNoMatchingCriteria_returnsEmpty() {
        // Параметры для фильтрации
        MyUserSearchFilterDTO filter = new MyUserSearchFilterDTO();
        filter.setEmail("non.existent@example.com");
        filter.setPage(1);

        // Проверяем, что пользователи не найдены по несуществующему email
        List<MyUser> users = myUserRepository.findByFilters(
                null,
                null,
                filter.getEmail(),
                null,
                PageRequest.of(filter.getPage() - 1, 10) // PAGE_SIZE = 10
        );

        assertTrue(users.isEmpty());
    }

    @Test
    void existsByEmailEqualsIgnoreCase_existingEmail_returnsTrue() {
        boolean exists = myUserRepository.existsByEmailEqualsIgnoreCase("john.doe@example.com");
        assertTrue(exists);
    }

    @Test
    void existsByEmailEqualsIgnoreCase_nonExistingEmail_returnsFalse() {
        boolean exists = myUserRepository.existsByEmailEqualsIgnoreCase("non.existent@example.com");
        assertFalse(exists);
    }

    @Test
    void findByEmailEqualsIgnoreCase_existingEmail_returnsUser() {
        Optional<MyUser> user = myUserRepository.findByEmailEqualsIgnoreCase("john.doe@example.com");
        assertTrue(user.isPresent());
        assertEquals("john.doe@example.com", user.get().getEmail());
    }

    @Test
    void findByEmailEqualsIgnoreCase_nonExistingEmail_returnsEmpty() {
        Optional<MyUser> user = myUserRepository.findByEmailEqualsIgnoreCase("non.existent@example.com");
        assertFalse(user.isPresent());
    }
}
