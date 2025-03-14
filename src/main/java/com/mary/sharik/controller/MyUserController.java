package com.mary.sharik.controller;


import com.mary.sharik.model.dto.request.MyUserRegisterDTO;
import com.mary.sharik.model.dto.request.MyUserUpdateDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.service.MyUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
