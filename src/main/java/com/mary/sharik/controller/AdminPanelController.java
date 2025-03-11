package com.mary.sharik.controller;

import com.mary.sharik.model.dto.SetProductStatusDTO;
import com.mary.sharik.model.entity.Cart;
import com.mary.sharik.service.CartService;
import com.mary.sharik.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminPanelController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping("/cart_of")
    public Cart.ActiveCart getActiveCart(@RequestParam @NotNull String userId) {
        return cartService.getActiveCartByUserId(userId);
    }

    @GetMapping("/history_of")
    public List<Cart.Order> getHistory(@RequestParam @NotNull String userId) {
        return cartService.getHistoryByUserId(userId);
    }

    @PostMapping("/set_product_status")
    public boolean setProductStatus(@RequestBody @Valid @NotNull SetProductStatusDTO dto) {
        productService.setProductStatus(dto);
        return true;
    }
}
