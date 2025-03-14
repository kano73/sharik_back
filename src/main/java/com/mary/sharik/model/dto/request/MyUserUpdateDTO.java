package com.mary.sharik.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MyUserUpdateDTO {
    @Size(min = 1, max = 500)
    private String firstName;

    @Size(min = 1, max = 500)
    private String lastName;

    @Size(min = 5, max = 500)
    private String password;

    @Size(min = 3, max = 500)
    private String email;

    @Size(min = 3, max = 500)
    private String address;

    @Override
    public String toString() {
        return "MyUserUpdateDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
