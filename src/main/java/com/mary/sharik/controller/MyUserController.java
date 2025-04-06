package com.mary.sharik.controller;

import com.mary.sharik.model.dto.request.MyUserRegisterDTO;
import com.mary.sharik.model.dto.request.MyUserUpdateDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.service.MyUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MyUserController {

    private final MyUserService myUserService;

    @PostMapping("/register")
    private String register (@RequestBody @Valid MyUserRegisterDTO dto){
        myUserService.register(dto);
        return "successfully registered";
    }

    @GetMapping("/profile")
    public MyUserPublicInfoDTO getProfile(){
        return myUserService.getUserInfo();
    }

    @PostMapping("/update_profile")
    public MyUserPublicInfoDTO updateProfile(@RequestBody @Valid MyUserUpdateDTO user){
        return myUserService.updateInfo(user);
    }

    @GetMapping("/is_user_admin")
    public boolean isUserAdmin(){
        return myUserService.isUserAdmin();
    }
}
