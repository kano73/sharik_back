package com.mary.sharik.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Product {
    @Id
    private String id;
    private String name;
    private BigDecimal price;
    private Integer amountLeft;
    private String description;
    private String imageUrl;
    private List<String> categories;
    private boolean isAvailable;
}
