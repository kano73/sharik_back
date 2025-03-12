package com.mary.sharik.controller;

import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products")
    public List<Product> getProducts(@RequestBody(required = false) @Valid ProductSearchFilterDTO dto) {
        return productService.getProductsByFilterOnPage(dto);
    }

    @PostMapping("/create_product")
    public Product addProduct(@RequestBody AddProductDTO dto) {
        return productService.create(dto);
    }
}

