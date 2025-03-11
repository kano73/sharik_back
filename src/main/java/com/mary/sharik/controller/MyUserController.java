package com.mary.sharik.controller;


import com.mary.sharik.model.dto.MyUserRegisterDTO;
import com.mary.sharik.service.MyUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
}
