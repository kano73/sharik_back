package com.mary.sharik.controller;

import com.mary.sharik.model.dto.ProductSearchDTO;
import com.mary.sharik.model.entity.Product;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class ProductController {

    @GetMapping("/products")
    public List<Product> getProducts(@ModelAttribute ProductSearchDTO dto) {
        return null;
    }
}

