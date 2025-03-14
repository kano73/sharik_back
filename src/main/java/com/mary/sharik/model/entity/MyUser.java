package com.mary.sharik.model.entity;

import com.mary.sharik.model.enums.RoleEnum;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@Document(collection = "users")
public class MyUser {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String password;
    private RoleEnum role;
}
