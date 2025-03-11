package com.mary.sharik.model.dto.request;

import com.mary.sharik.model.enums.SortProductByEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.List;

@Getter
@Setter
public class ProductSearchFilterDTO {
    @Size(min = 2, max = 200,message = "name length must be greater than 2 and less than 200")
    private String nameAndDescription;

    @DecimalMin("0.01")
    private Double priceFrom;

    private Double priceTo;

    @Size(min = 1, message = "There must be at least one category")
    private List<String> categories;

    @Min(value = 1, message = "page number must be grater than 0")
    private Integer page = 1;

    @Enumerated(EnumType.STRING)
    private SortProductByEnum sortBy = SortProductByEnum.NAME;

    private Sort.Direction sortDirection = Sort.Direction.ASC;

    @Override
    public String toString() {
        return "ProductSearchFilterDTO{" +
                "nameOrDescription='" + nameAndDescription + '\'' +
                ", priceFrom=" + priceFrom +
                ", priceTo=" + priceTo +
                ", categories=" + categories +
                ", page=" + page +
                ", sortBy=" + sortBy +
                ", sortDirection=" + sortDirection +
                '}';
    }
}
