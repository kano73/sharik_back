package com.mary.sharik.controller;

import com.mary.sharik.kafka.KafkaCartService;
import com.mary.sharik.kafka.KafkaHistoryService;
import com.mary.sharik.kafka.KafkaProductService;
import com.mary.sharik.model.dto.request.AddProductDTO;
import com.mary.sharik.model.dto.request.MyUserSearchFilterDTO;
import com.mary.sharik.model.dto.request.SetProductStatusDTO;
import com.mary.sharik.model.dto.responce.MyUserPublicInfoDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.service.MyUserService;
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

    private final MyUserService myUserService;
    private final KafkaCartService kafkaCartService;
    private final KafkaHistoryService kafkaHistoryService;
    private final KafkaProductService kafkaProductService;

//    single

    @GetMapping("/profile_of")
    public MyUserPublicInfoDTO getUsersInfo(@RequestParam @NotBlank String id) {
        return myUserService.getUsersInfoById(id);
    }

    @GetMapping("/cart_of")
    public List<ProductAndQuantity> getActiveCart(@RequestParam @NotBlank String id) {
        return kafkaCartService.getCartOfUserById(id);
    }

    @GetMapping("/history_of")
    public OrdersHistory getHistory(@RequestParam @NotBlank String id) {
        return kafkaHistoryService.getOrdersHistoryByUserId(id);
    }

//    multiple

    @GetMapping("/all_histories")
    public List<OrdersHistory> getAllHistory(@RequestParam @DefaultValue("1") @Min(1) Integer page) {
        return kafkaHistoryService.getWholeHistory(page);
    }

    @PostMapping("/all_users")
    public List<MyUserPublicInfoDTO> getAllUsers(@RequestBody MyUserSearchFilterDTO filter) {
        return myUserService.getUsersByFilters(filter);
    }

//    action

    @PostMapping("/set_product_status")
    public boolean setProductStatus(@RequestBody @Valid @NotNull SetProductStatusDTO dto) {
        return kafkaProductService.setProductStatus(dto);
    }

    @PostMapping("/create_product")
    public Boolean addProduct(@RequestBody AddProductDTO dto) {
        return kafkaProductService.createProduct(dto);
    }
}
