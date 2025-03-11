package com.mary.sharik.model.jwt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String usernameOrEmail;
    private String password;
}
