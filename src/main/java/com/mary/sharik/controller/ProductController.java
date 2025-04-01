package com.mary.sharik.controller;

import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.service.ProductService;
import com.mary.sharik.kafka.KafkaProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductService productService;
    private final KafkaProductService kafkaProductService;

    @PostMapping("/products")
    public List<Product> getProducts(@RequestBody(required = false) @Valid ProductSearchFilterDTO dto) throws Exception {
//        return productService.getProductsByFilterOnPage(dto);

        System.out.println("got request");

        List<Product> products = kafkaProductService.requestProductsByFilter(dto);

        System.out.println(products);

        return products;

    }


    @GetMapping("/product")
    public Product getProduct(@RequestParam String id) throws Exception {
//        return productService.findById(id);

        return kafkaProductService.requestProductsById(id);
    }

}

