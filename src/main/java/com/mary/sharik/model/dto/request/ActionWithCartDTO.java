package com.mary.sharik.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionWithCartDTO {
    private String productId;
    private int quantity;

    @Override
    public String toString() {
        return "ActionWithCartDTO{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
