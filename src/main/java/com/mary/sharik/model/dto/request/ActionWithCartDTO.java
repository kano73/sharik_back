package com.mary.sharik.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionWithCartDTO {
    @NotNull
    private String productId;
    @NotNull
    @Min(0)
    private int quantity;

    @Override
    public String toString() {
        return "ActionWithCartDTO{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
