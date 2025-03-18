package com.mary.sharik.controller;

import com.mary.sharik.model.dto.request.ActionWithCartDTO;
import com.mary.sharik.model.dto.request.OrderRequest;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.service.CartService;
import com.mongodb.lang.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @DeleteMapping("/empty_cart")
    public boolean emptyCart() {
        cartService.emptyCart();
        return true;
    }

    @PostMapping("/add")
    public boolean addItem(@RequestBody ActionWithCartDTO actionWithCartDTO) {
        cartService.addToCart(actionWithCartDTO);
        return true;
    }

    @PostMapping("/change_amount")
    public boolean reduceAmount(@RequestBody @Valid ActionWithCartDTO actionWithCartDTO) {
        cartService.resetAmountOrDelete(actionWithCartDTO);
        return true;
    }

    @PostMapping("/make_order")
    public boolean completeOrder(@RequestBody @Valid OrderRequest orderRequest) {
        cartService.makeOrder(orderRequest.getCustomAddress());
        return true;
    }

    @GetMapping("/cart")
    public List<ProductAndQuantity> getActiveCart() {
        return cartService.getCart();
    }

    @GetMapping("/history")
    public List<OrdersHistory.Order> getHistory() {
        return cartService.getOrdersHistory();
    }
}
