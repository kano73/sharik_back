package com.mary.sharik.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyUserRegisterDTO {
    @Size(min = 1, max = 500)
    private String firstName;

    @Size(min = 1, max = 500)
    private String lastName;

    @NotNull
    @NotBlank
    @Size(min = 5, max = 50)
    private String password;

    @NotNull
    @NotBlank
    @Size(min = 3, max = 200)
    private String email;

    private String address;
}
