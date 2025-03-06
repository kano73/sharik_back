package com.mary.sharik.service;

import com.mary.sharik.model.entity.Product;
import com.mary.sharik.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Value("page.size.product")
    private Integer pageSize;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProductsOnPage(int page) {
        return productRepository.findAll(PageRequest.of(page-1, pageSize)).getContent();
    }
}

