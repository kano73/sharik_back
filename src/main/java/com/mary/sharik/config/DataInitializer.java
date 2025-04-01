package com.mary.sharik.config;

import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.RoleEnum;
import com.mary.sharik.repository.MyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner {

    @Value("${admin.password}")
    private String password;
    private final MyUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        addAdmin();
    }

    private void addAdmin() {
        String adminMail = "admin.main@mail.com";
        if (userRepository.findByEmailEqualsIgnoreCase(adminMail).isEmpty()) {
            MyUser admin = new MyUser();
            admin.setFirstName("MAIN-ADMIN");
            admin.setEmail(adminMail);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(RoleEnum.ADMIN);
            userRepository.save(admin);
        }
    }

}
