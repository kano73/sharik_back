package com.mary.sharik.controller;

import com.mary.sharik.model.dto.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.service.ProductService;
import com.mongodb.lang.Nullable;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products")
    public List<Product> getProducts(@RequestBody(required = false) @Valid ProductSearchFilterDTO dto) {
        return productService.getProductsByFilterOnPage(dto);
    }
}

