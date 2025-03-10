package com.mary.sharik.controller;

import com.mary.sharik.model.dto.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> getProducts(@ModelAttribute @Valid ProductSearchFilterDTO dto) {
        return productService.getProductsByFilterOnPage(dto);
    }
}

