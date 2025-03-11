package com.mary.sharik.controller;

import com.mary.sharik.model.dto.ActionWithCartDTO;
import com.mary.sharik.model.entity.Cart;
import com.mary.sharik.service.CartService;
import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        cartService.addItemToCart(actionWithCartDTO.getProductId(), actionWithCartDTO.getQuantity());
        return true;
    }

    @DeleteMapping("/remove")
    public boolean reduceAmount(@RequestBody ActionWithCartDTO actionWithCartDTO) {
        cartService.reduceAmountOrDelete(actionWithCartDTO);
        return true;
    }

    @PostMapping("/make_order")
    public boolean completeOrder(@RequestBody @Nullable String customAddress) {
        cartService.makeOrder(customAddress);
        return true;
    }

    @GetMapping("/my_cart")
    public Cart.ActiveCart getActiveCart() {
        return cartService.getActiveCart();
    }

    @GetMapping("/my_history")
    public List<Cart.Order> getHistory() {
        return cartService.getHistory();
    }
}
