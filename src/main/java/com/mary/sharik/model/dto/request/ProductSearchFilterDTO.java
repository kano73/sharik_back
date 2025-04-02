package com.mary.sharik.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mary.sharik.model.enumClass.SortProductBy;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductSearchFilterDTO {
    @Size(max = 200, message = "name length must be greater than 2 and less than 200")
    private String nameAndDescription ;

    private Double priceFrom ;

    private Double priceTo ;

    private List<String> categories ;

    @Min(value = 1, message = "page number must be grater than 0")
    private Integer page ;

    @Enumerated(EnumType.STRING)
    private SortProductBy sortBy ;

    @Enumerated(EnumType.STRING)
    private Sort.Direction sortDirection;

    @JsonCreator
    public ProductSearchFilterDTO(
            @JsonProperty("nameAndDescription") String nameAndDescription,
            @JsonProperty("categories") List<String> categories,
            @JsonProperty("page") Integer page,
            @JsonProperty("sortBy") SortProductBy sortBy,
            @JsonProperty("sortDirection") Sort.Direction sortDirection) {

        this.nameAndDescription = (nameAndDescription == null || nameAndDescription.isEmpty()) ? "" : nameAndDescription;
//price adjustment is in product_microservice
        this.categories = (categories == null) ? new ArrayList<>() : categories;
        this.page = (page == null || page < 1) ? 1 : page;
        this.sortBy = (sortBy == null) ? SortProductBy.NAME : sortBy;
        this.sortDirection = (sortDirection == null) ? Sort.Direction.ASC : sortDirection;
    }

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
}
