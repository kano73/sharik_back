package com.mary.sharik.model.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class ProductSearchDTO {
    @Size(min = 2, max = 200,message = "name length must be greater than 2 and less than 200")
    private String name;
    private Double priceFrom;
    private Double priceTo;
    private Double amountLeft;
    private String description;
    private String imageUrl;
    private List<String> categories;
}
