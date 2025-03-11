package com.mary.sharik.model.dto.storage;

import com.mary.sharik.model.entity.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAndQuantity {
    private Product product;
    private int quantity;
}