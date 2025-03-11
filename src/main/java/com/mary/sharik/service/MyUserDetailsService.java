package com.mary.sharik.service;

import com.mary.sharik.model.details.MyUserDetails;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.repository.MyUserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final MyUserRepository myUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser myUser = myUserRepository.findByUsernameEqualsIgnoreCase(username)
                .or(() -> myUserRepository.findByEmailEqualsIgnoreCase(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new MyUserDetails(myUser);
    }
}

