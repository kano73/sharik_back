package com.mary.sharik.model.entity;

import com.mary.sharik.model.enumClass.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
@Table(name="my_user")
public class MyUser {
    @Id
    @UuidGenerator
    private String id;

    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String password;
    private Role role;
}
