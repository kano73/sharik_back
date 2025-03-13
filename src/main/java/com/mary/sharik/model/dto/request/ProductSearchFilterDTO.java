package com.mary.sharik.model.dto.request;

import com.mary.sharik.exceptions.ValidationFailedException;
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
    @Size(max = 200, message = "name length must be greater than 2 and less than 200")
    private String nameAndDescription;

    @DecimalMin("0.01")
    private Double priceFrom;

    @DecimalMin("0.01")
    private Double priceTo;

    private List<String> categories;

    @Min(value = 1, message = "page number must be grater than 0")
    private Integer page = 1;

    @Enumerated(EnumType.STRING)
    private SortProductByEnum sortBy = SortProductByEnum.NAME;

    @Enumerated(EnumType.STRING)
    private Sort.Direction sortDirection = Sort.Direction.ASC;

    @Override
    public String toString() {
        return "ProductSearchFilterDTO{" +
                "nameAndDescription='" + nameAndDescription + '\'' +
                ", priceFrom=" + priceFrom +
                ", priceTo=" + priceTo +
                ", categories=" + categories +
                ", page=" + page +
                ", sortBy=" + sortBy +
                ", sortDirection=" + sortDirection +
                '}';
    }

    public void validate() {
        if (nameAndDescription != null && nameAndDescription.length() < 3) {
            if (nameAndDescription.isEmpty()){
                nameAndDescription=null;
            }else{
                throw new ValidationFailedException("name filed must be greater than 2");
            }
        }
        if (categories.isEmpty()){
            categories=null;
        }
    }
}
