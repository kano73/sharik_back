package com.mary.sharik.model.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyUserSearchFilterDTO {
    private String firstOrLastName = "";
    private String email = "";
    @Min(1)
    private Integer page = 1;

    @Override
    public String toString() {
        return "MyUserSearchFilterDTO{" +
                "firstOrLastName='" + firstOrLastName + '\'' +
                ", email='" + email + '\'' +
                ", page=" + page +
                '}';
    }
}