package com.mary.sharik.repository;

import com.mary.sharik.model.entity.MyUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyUserRepository extends CrudRepository<MyUser, String> {
    @Query("""
        SELECT u FROM MyUser u
        WHERE (:firstName IS NULL OR :firstName = '' OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
           AND (:lastName IS NULL OR :lastName = '' OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
           AND (:email IS NULL OR :email = '' OR u.email LIKE CONCAT('%', :email, '%'))
           AND (:id IS NULL OR :id = '' OR u.id = :id)
    """)
    List<MyUser> findByFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("id") String id,
            Pageable pageable
    );

    boolean existsByEmailEqualsIgnoreCase(String email);

    Optional<MyUser> findByEmailEqualsIgnoreCase(String email);
}
