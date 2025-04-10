package com.mary.sharik.controller;

import com.mary.sharik.kafka.KafkaProductService;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProductController {

    private final KafkaProductService kafkaProductService;

    @PostMapping("/products")
    public List<Product> getProducts(@RequestBody(required = false) @Valid ProductSearchFilterDTO dto) {
        return kafkaProductService.requestProductsByFilter(dto);
    }

    @GetMapping("/product")
    public Product getProduct(@RequestParam String id) {
        return kafkaProductService.requestProductsById(id);
    }
}

