package com.mary.sharik.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddProductDTO {
    @NotBlank
    @Size(min = 10, max = 100)
    private String name;
    @DecimalMin("0,5")
    private Double price;
    private Integer amountLeft;
    @NotBlank
    @Size(min = 10, max = 2000)
    private String description;
    private String imageUrl;
    private List<String> categories;
}
