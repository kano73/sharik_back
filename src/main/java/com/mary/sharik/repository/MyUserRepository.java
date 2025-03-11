package com.mary.sharik.repository;

import com.mary.sharik.model.entity.MyUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyUserRepository extends MongoRepository<MyUser, String> {
    boolean existsByEmailEqualsIgnoreCase(String email);

    Optional<MyUser> findByEmailEqualsIgnoreCase(String email);
}
