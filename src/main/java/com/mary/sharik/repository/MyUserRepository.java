package com.mary.sharik.repository;

import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.RoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyUserRepository extends MongoRepository<MyUser, String> {
    boolean existsByEmailEqualsIgnoreCase(String email);

    Optional<MyUser> findByEmailEqualsIgnoreCase(String email);

//    todo: not working
    @Query(value = "{ $and: [ "
            + " { $or: [ "
            + "   { 'firstName': { $regex: ?0, $options: 'i' } }, "
            + "   { 'lastName': { $regex: ?0, $options: 'i' } }, "
            + "   { '?0': { $eq: '' } } "
            + " ] }, "
            + " { $or: [ "
            + "   { 'email': { $regex: ?1, $options: 'i' } }, "
            + "   { '?1': { $eq: '' } } "
            + " ] } "
            + "] }",
            fields = "{ 'id': 1, 'firstName': 1, 'lastName': 1, 'address': 1, 'email': 1, 'role': 1 }")
    Page<MyUserPublicInfoDTO> findByFilters(String firstOrLastName,
                                            String email,
                                            Pageable pageable);

}
