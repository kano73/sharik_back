package com.mary.sharik.service;

import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.PriceProperties;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.ProductSearchFilterDTO;
import com.mary.sharik.model.dto.request.SetProductStatusDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.repository.ProductRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Sort;


import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    @Value("${page.size.product}")
    private Integer PAGE_SIZE;
    private final Integer DIGITS_AFTER_COMA = PriceProperties.AFTER_COMA;

    private final ProductRepository productRepository;

    public List<Product> getProductsByFilterOnPage(@NotNull ProductSearchFilterDTO dto) {
        Integer priceFrom = dto.getPriceFrom()==null? null : dto.getPriceFrom().intValue();
        Integer priceTo = dto.getPriceTo()==null? null : dto.getPriceTo().intValue();

        return productRepository.searchProductsByFilter(
                dto.getNameAndDescription(),
                priceFrom,
                priceTo,
                dto.getCategories(),
                PageRequest.of(
                        dto.getPage()-1,
                        PAGE_SIZE,
                        Sort.by(dto.getSortDirection() ,dto.getSortBy().toString().toLowerCase()))
        ).getContent();
    }

    public void setProductStatus(SetProductStatusDTO dto) {
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(
                ()-> new NoDataFoundException("no product found with id: "+dto.getProductId())
        );
        product.setAvailable(dto.getStatus());
        productRepository.save(product);
    }

    public Product create(AddProductDTO dto) {
        Product product = new Product();
        product.setAvailable(false);
        product.setCategories(dto.getCategories());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setAmountLeft(dto.getAmountLeft());
        product.setImageUrl(dto.getImageUrl());

        product.setPrice((int) Math.round(dto.getPrice()) * (int) Math.pow(10,DIGITS_AFTER_COMA));

        return productRepository.save(product);
    }

    public Product findById(String id) {
        return productRepository.findById(id).orElseThrow(
                () -> new NoDataFoundException("no product found with id: "+id)
        );
    }
}