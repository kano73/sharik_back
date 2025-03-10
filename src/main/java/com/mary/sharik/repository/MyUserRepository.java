package com.mary.sharik.repository;

import com.mary.sharik.model.entity.MyUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MyUserRepository extends MongoRepository<MyUser, String> {

    Optional<MyUser> findByUsernameEqualsIgnoreCase(String username);
}
