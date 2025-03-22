package com.mary.sharik.service;

import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.dto.request.MyUserRegisterDTO;
import com.mary.sharik.model.dto.request.MyUserSearchFilterDTO;
import com.mary.sharik.model.dto.request.MyUserUpdateDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.RoleEnum;
import com.mary.sharik.repository.MyUserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class MyUserService {
    @Value("${page.size.product}")
    private Integer PAGE_SIZE;

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

    public MyUserPublicInfoDTO updateInfo(@Valid MyUserUpdateDTO dto) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());

        return MyUserPublicInfoDTO.fromUser(myUserRepository.save(user));
    }

    public List<MyUserPublicInfoDTO> getUsersByFilters(@NotNull MyUserSearchFilterDTO filter) {
        List<MyUserPublicInfoDTO> byFilters;

        if(filter.getFirstOrLastName()!=null && !Objects.equals(filter.getFirstOrLastName(), "")){
            byFilters = myUserRepository.findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(
                    filter.getFirstOrLastName(),
                    filter.getFirstOrLastName(),
                    PageRequest.of(filter.getPage()-1 , PAGE_SIZE)).getContent();

        }else if(filter.getEmail()!=null && !Objects.equals(filter.getEmail(), "")){
            byFilters = myUserRepository.findByEmail(filter.getEmail()).stream().toList();

        } else if(filter.getId()!=null && !Objects.equals(filter.getId(), "")){
            byFilters = myUserRepository.findById(filter.getId()).stream()
                    .map(MyUserPublicInfoDTO::fromUser).toList();

        }else{
            byFilters = myUserRepository.findAll(PageRequest.of(filter.getPage()-1 , PAGE_SIZE)).stream().map(MyUserPublicInfoDTO::fromUser).toList();
        }
        return byFilters;
    }

    public boolean isUserAdmin() {
        try {
            return authenticatedMyUserService.getCurrentUserAuthenticated().getRole().equals(RoleEnum.ADMIN);
        }
        catch (NoDataFoundException e) {
            return false;
        }
    }
}
