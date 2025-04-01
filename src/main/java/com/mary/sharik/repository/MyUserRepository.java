package com.mary.sharik.repository;

import com.mary.sharik.model.dto.request.MyUserSearchFilterDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.entity.MyUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface MyUserRepository extends MongoRepository<MyUser, String> {
    boolean existsByEmailEqualsIgnoreCase(String email);

    Optional<MyUser> findByEmailEqualsIgnoreCase(String email);

    @Query(fields = "{ 'id': 1, 'firstName': 1, 'lastName': 1, 'address': 1, 'email': 1, 'role': 1 }")
    Page<MyUserPublicInfoDTO>  findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(String lastName, String firstName, Pageable pageable);

    @Query("{$and: [ " +
            "?#{ [0].id != null ? {_id: [0].id} : {} }, " +
            "?#{ [0].firstOrLastName != null && [0].firstOrLastName != '' ? " +
            "{$or: [ " +
            "{firstName: {$regex: [0].firstOrLastName, $options: 'i'}}, " +
            "{lastName: {$regex: [0].firstOrLastName, $options: 'i'}} " +
            "]} : {} }, " +
            "?#{ [0].email != null && [0].email != '' ? {email: [0].email} : {} }" +
            "]}")
    Page<MyUser> findByFilters(MyUserSearchFilterDTO filter, Pageable pageable);
}
