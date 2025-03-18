package com.mary.sharik.controller;

import com.mary.sharik.model.dto.request.MyUserSearchFilterDTO;
import com.mary.sharik.model.dto.request.SetProductStatusDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.service.CartService;
import com.mary.sharik.service.MyUserService;
import com.mary.sharik.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminPanelController {

    private final CartService cartService;
    private final ProductService productService;
    private final MyUserService myUserService;

//    single

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
        return cartService.getOrdersHistoryByUserId(userId);
    }

//    multiple

    @GetMapping("/all_histories")
    public List<OrdersHistory> getAllHistory(@RequestParam @DefaultValue("1") @Min(1) Integer page) {
        return cartService.getWholeHistory(page);
    }

    @PostMapping("/all_users")
    public List<MyUserPublicInfoDTO> getAllUsers(@RequestBody MyUserSearchFilterDTO filter) {
        System.out.println(filter);
        List<MyUserPublicInfoDTO> usersByFilters = myUserService.getUsersByFilters(filter);
        System.out.println(usersByFilters);
        return usersByFilters;
    }

//    action

    @PostMapping("/set_product_status")
    public boolean setProductStatus(@RequestBody @Valid @NotNull SetProductStatusDTO dto) {
        productService.setProductStatus(dto);
        return true;
    }
}
