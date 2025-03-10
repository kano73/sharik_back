package com.mary.sharik.service;

import com.mary.sharik.model.dto.ProductSearchFilterDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Sort;


import java.util.List;

@Service
public class ProductService {
    @Value("${page.size.product}")
    private Integer pageSize;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProductsByFilterOnPage(ProductSearchFilterDTO dto) {
        dto.setNameOrDescription(
                dto.getNameOrDescription() == null || dto.getNameOrDescription().isEmpty() ?
                ""
                : dto.getNameOrDescription());

        return productRepository.searchProductsByFilter(
                dto.getNameOrDescription(),
                dto.getPriceFrom(),
                dto.getPriceTo(),
                dto.getCategories(),
                PageRequest.of(
                        dto.getPage()-1,
                        pageSize,
                        Sort.by(dto.getSortDirection() ,dto.getSortBy().toString()))
        ).getContent();
    }
}

