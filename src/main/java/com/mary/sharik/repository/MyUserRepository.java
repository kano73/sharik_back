package com.mary.sharik.repository;

import com.mary.sharik.model.entity.MyUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MyUserRepository extends CrudRepository<MyUser, String> {
    boolean existsByEmailEqualsIgnoreCase(String email);

    Optional<MyUser> findByEmailEqualsIgnoreCase(String email);

    List<MyUser> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrId
            (String firstName, String lastName, String email, String id, Pageable pageable);
}
