package com.mary.sharik.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyUserRegisterDTO {
    @NotBlank
    @Size(min = 2, max = 20)
    private String username;
    @NotBlank
    @Size(min = 2, max = 20)
    private String password;
    @NotBlank
    @Size(min = 3, max = 200)
    private String email;
}
