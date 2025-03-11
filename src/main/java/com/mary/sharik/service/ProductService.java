package com.mary.sharik.service;

import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.dto.ProductSearchFilterDTO;
import com.mary.sharik.model.dto.SetProductStatusDTO;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.repository.ProductRepository;
import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Sort;


import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    @Value("${page.size.product}")
    private Integer pageSize;

    private final ProductRepository productRepository;

    public List<Product> getProductsByFilterOnPage(ProductSearchFilterDTO dto) {
        if (dto == null) {
            dto = new ProductSearchFilterDTO();
        }
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

    public void setProductStatus(SetProductStatusDTO dto) {
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(
                ()-> new NoDataFoundException("no product found with id: "+dto.getProductId())
        );
        product.setAvailable(dto.getStatus());
        productRepository.save(product);
    }
}

