package com.mary.sharik.controller;

import com.mary.sharik.kafka.KafkaCartService;
import com.mary.sharik.kafka.KafkaHistoryService;
import com.mary.sharik.model.dto.request.ActionWithCartDTO;
import com.mary.sharik.model.dto.request.OrderDetailsDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final KafkaCartService kafkaCartService;
    private final KafkaHistoryService kafkaHistoryService;

//    action


    @DeleteMapping("/empty_cart")
    public boolean emptyCart() {
        return kafkaCartService.emptyCart();
//        cartService.emptyCart();
//        return true;
    }

    @PostMapping("/add")
    public boolean addItem(@RequestBody @Valid ActionWithCartDTO actionWithCartDTO) {
        return kafkaCartService.addToCart(actionWithCartDTO);
//        cartService.addToCart(actionWithCartDTO);
//        return true;
    }

    @PostMapping("/change_amount")
    public boolean changeAmount(@RequestBody @Valid ActionWithCartDTO actionWithCartDTO) {
        return kafkaCartService.changeAmount(actionWithCartDTO);
//        cartService.resetAmountOrDelete(actionWithCartDTO);
//        return true;
    }

    @PostMapping("/make_order")
    public boolean completeOrder(@RequestBody @Valid OrderDetailsDTO orderDetailsDTO) {
        return kafkaCartService.makeOrder(orderDetailsDTO);
//        cartService.makeOrder(orderDetailsDTO.getCustomAddress());
//        return true;
    }

//view
    @GetMapping("/cart")
    public List<ProductAndQuantity> getActiveCart() {
        return kafkaCartService.findCart();
//        return cartService.getCart();
    }

    @GetMapping("/history")
    public OrdersHistory getHistory() {
        return kafkaHistoryService.findHistory();
//        return cartService.getOrdersHistory();
    }
}
