package com.mary.sharik.controller;

import com.mary.sharik.kafka.KafkaCartService;
import com.mary.sharik.model.dto.request.ActionWithCartDTO;
import com.mary.sharik.model.dto.request.OrderDetailsDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final KafkaCartService kafkaCartService;

    @DeleteMapping("/empty_cart")
    public boolean emptyCart() {
        return kafkaCartService.emptyCart();
    }

    @PostMapping("/add")
    public boolean addItem(@RequestBody @Valid ActionWithCartDTO actionWithCartDTO) {
        return kafkaCartService.addToCart(actionWithCartDTO);
    }

    @PostMapping("/change_amount")
    public boolean changeAmount(@RequestBody @Valid ActionWithCartDTO actionWithCartDTO) {
        return kafkaCartService.changeAmount(actionWithCartDTO);
    }

    @PostMapping("/make_order")
    public boolean completeOrder(@RequestBody @Valid OrderDetailsDTO orderDetailsDTO) {
        return kafkaCartService.makeOrder(orderDetailsDTO);
    }

//view
    @GetMapping("/cart")
    public List<ProductAndQuantity> getActiveCart() {
        return kafkaCartService.findCart();
    }
}
