package com.mary.sharik.service;

import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.dto.MyUserRegisterDTO;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.RoleEnum;
import com.mary.sharik.repository.MyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MyUserService {

    private final MyUserValidationService myUserValidationService;
    private final MyUserRepository myUserRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(MyUserRegisterDTO dto){
        MyUser myUser = new MyUser();
        myUser.setUsername(dto.getUsername());
        myUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        myUser.setRole(RoleEnum.USER);

        myUserValidationService.credentialsUniqueOrThrow(myUser);

        myUserRepository.save(myUser);
    }

    public MyUser findById(String userId) {
        return myUserRepository.findById(userId).orElseThrow(()->
                new NoDataFoundException("No user found with id:" + userId));
    }

    public MyUser findByUsernameOrEmail(String usernameOrEmail) {
        return myUserRepository.findByUsernameEqualsIgnoreCase(usernameOrEmail)
                .or(() -> myUserRepository.findByEmailEqualsIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + usernameOrEmail));
    }
}
