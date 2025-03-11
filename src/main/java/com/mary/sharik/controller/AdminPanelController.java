package com.mary.sharik.controller;

import com.mary.sharik.model.dto.request.SetProductStatusDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.service.CartService;
import com.mary.sharik.service.MyUserService;
import com.mary.sharik.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    private final MyUserService myUserService;

    @GetMapping("/profile_of")
    public MyUserPublicInfoDTO getUsersInfo(@RequestParam @NotBlank String userId) {
        return myUserService.getUsersInfoById(userId);
    }

    @GetMapping("/cart_of")
    public List<ProductAndQuantity> getActiveCart(@RequestParam @NotBlank String userId) {
        return cartService.getCartByUserId(userId);
    }

    @GetMapping("/history_of")
    public List<OrdersHistory.Order> getHistory(@RequestParam @NotBlank String userId) {
        return cartService.getHistoryByUserId(userId);
    }

    @GetMapping("/all_history")
    public List<OrdersHistory> getAllHistory(@RequestParam Integer page) {
        return cartService.getWholeHistory(page);
    }

    @PostMapping("/set_product_status")
    public boolean setProductStatus(@RequestBody @Valid @NotNull SetProductStatusDTO dto) {
        productService.setProductStatus(dto);
        return true;
    }
}
