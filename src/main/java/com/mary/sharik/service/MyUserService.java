package com.mary.sharik.service;

import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.dto.request.MyUserRegisterDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.RoleEnum;
import com.mary.sharik.repository.MyUserRepository;
import jakarta.validation.constraints.NotBlank;
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
    private final AuthenticatedMyUserService authenticatedMyUserService;

    public void register(MyUserRegisterDTO dto){
        MyUser myUser = new MyUser();
        myUser.setFirstName(dto.getFirstName());
        myUser.setLastName(dto.getLastName());
        myUser.setEmail(dto.getEmail());
        myUser.setAddress(dto.getAddress());
        myUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        myUser.setRole(RoleEnum.USER);

        myUserValidationService.credentialsUniqueOrThrow(myUser);

        myUserRepository.save(myUser);
    }

    public MyUser findById(String userId) {
        return myUserRepository.findById(userId).orElseThrow(()->
                new NoDataFoundException("No user found with id:" + userId));
    }

    public MyUser findByEmail(String email) {
        return myUserRepository.findByEmailEqualsIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + email));
    }

    public MyUserPublicInfoDTO getUserInfo(){
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        return getUsersInfoById(user.getId());
    }

    public MyUserPublicInfoDTO getUsersInfoById(@NotBlank String userId) {
        MyUser myUser = myUserRepository.findById(userId).orElseThrow(
                () -> new NoDataFoundException("no user found with id:" + userId)
        );
        return MyUserPublicInfoDTO.fromUser(myUser);
    }

}
