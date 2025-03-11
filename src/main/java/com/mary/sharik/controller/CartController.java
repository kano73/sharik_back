package com.mary.sharik.controller;

import com.mary.sharik.model.entity.Cart;
import com.mary.sharik.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public void addItem(@RequestParam String productId, @RequestParam int quantity) {
        cartService.addItemToCart(productId, quantity);
    }

    @PostMapping("/order")
    public void completeOrder(@RequestBody String address) {
        cartService.completeOrder(address);
    }

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable String userId) {
        return cartService.getOrCreateCart(userId);
    }
}
