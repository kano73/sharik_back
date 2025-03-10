package com.mary.sharik.service;

import com.mary.sharik.exceptions.CredentialsNotUniqueExceptions;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.repository.MyUserRepository;
import org.springframework.stereotype.Service;

@Service
public class MyUserValidationService {
    private final MyUserRepository myUserRepository;

    public MyUserValidationService(MyUserRepository myUserRepository) {
        this.myUserRepository = myUserRepository;
    }

    public void credentialsUniqueOrThrow(MyUser myUser) {
        if(myUserRepository.existsByUsername(myUser.getUsername())) {
            throw new CredentialsNotUniqueExceptions("username already exists");
        }
    }
}
