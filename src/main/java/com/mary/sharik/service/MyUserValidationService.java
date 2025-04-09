package com.mary.sharik.service;

import com.mary.sharik.exception.CredentialsNotUniqueExceptions;
import com.mary.sharik.exception.NoDataFoundException;
import com.mary.sharik.exception.ValidationFailedException;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.repository.MyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MyUserValidationService {
    private final MyUserRepository myUserRepository;

    public void credentialsUniqueOrThrow(MyUser myUser) {
        if(myUserRepository.existsByEmailEqualsIgnoreCase(myUser.getEmail())){
            throw new CredentialsNotUniqueExceptions("email already in use");
        }
    }

    public void isEmailValid(OAuth2User oAuth2User){
        String email = (String) oAuth2User.getAttributes().get("email");

        if (email == null) {
            throw new ValidationFailedException("");
        }

        myUserRepository.findByEmailEqualsIgnoreCase(email).orElseThrow(()->
                new NoDataFoundException("No user with email: "+ email));
    }
}
