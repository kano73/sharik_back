package com.mary.sharik.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailsDTO {
    private String userId;
    @NotBlank
    private String customAddress;
}


