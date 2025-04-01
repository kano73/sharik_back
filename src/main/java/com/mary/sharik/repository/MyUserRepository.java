package com.mary.sharik.repository;

import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.entity.MyUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface MyUserRepository extends MongoRepository<MyUser, String> {
    boolean existsByEmailEqualsIgnoreCase(String email);

    Optional<MyUser> findByEmailEqualsIgnoreCase(String email);

    @Query(fields = "{ 'id': 1, 'firstName': 1, 'lastName': 1, 'address': 1, 'email': 1, 'role': 1 }")
    Page<MyUserPublicInfoDTO>  findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(String lastName, String firstName, Pageable pageable);

}
