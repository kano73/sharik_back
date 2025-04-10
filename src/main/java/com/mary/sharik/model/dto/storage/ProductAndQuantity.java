package com.mary.sharik.model.dto.storage;

import com.mary.sharik.model.entity.Product;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class ProductAndQuantity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Product product;
    private int quantity;

    @Override
    public String toString() {
        return "ProductAndQuantity{" +
                "product=" + product +
                ", quantity=" + quantity +
                '}';
    }
}