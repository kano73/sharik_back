package com.mary.sharik.config;

import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enums.RoleEnum;
import com.mary.sharik.repository.MyUserRepository;
import com.mary.sharik.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    @Value("${admin.password}")
    private String password;

    private final MyUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        addAdmin();
        addProducts();
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

    private void addProducts() {
        if(productRepository.count() > 0){
            return;
        }
        Random random = new Random();
        List<String> categories = Arrays.asList("house", "tea", "sport", "computer", "science");
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setDescription("Description " + i);
            product.setPrice(random.nextInt(1000));
            product.setAmountLeft(random.nextInt(10));
            product.setAvailable(true);

            List<String> categoriesOfProduct = new ArrayList<>();
            for (String category : categories) {
                if(random.nextInt(5)>3){
                    categoriesOfProduct.add(category);
                }
            }
            product.setCategories(categoriesOfProduct);

            products.add(product);
        }
        productRepository.saveAll(products);
    }
}
