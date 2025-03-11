package com.mary.sharik.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private Integer price;
    private Integer amountLeft;
    private String description;
    private String imageUrl;
    private List<String> categories;
    private boolean isAvailable;
}
