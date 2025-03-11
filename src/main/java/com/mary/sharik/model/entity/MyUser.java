package com.mary.sharik.model.entity;

import com.mary.sharik.model.enums.RoleEnum;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "users")
public class MyUser {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String Address;
    private String email;
    private String password;
    private RoleEnum role;
}
