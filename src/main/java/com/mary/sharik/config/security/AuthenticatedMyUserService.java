package com.mary.sharik.config.security;

import com.mary.sharik.exception.NoDataFoundException;
import com.mary.sharik.model.details.MyUserDetails;
import com.mary.sharik.model.entity.MyUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedMyUserService {
    public MyUser getCurrentUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NoDataFoundException("Please login");
        }

        if (authentication.getPrincipal() instanceof MyUserDetails(MyUser myUser)) {
            return myUser;
        } else {
            throw new NoDataFoundException("Please login");
        }
    }
}