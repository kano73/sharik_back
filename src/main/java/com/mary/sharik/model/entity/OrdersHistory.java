package com.mary.sharik.model.entity;

import com.mary.sharik.model.enumClass.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrdersHistory {

    private String userId;
    private List<Order> orders = new ArrayList<>();

    @Data
    public static class Order{
        private List<CartItem> items;
        private LocalDateTime createdAt;
        private String orderId;
        private OrderStatus status;
        private String deliveryAddress;
    }

    @Data
    public static class CartItem {
        private Product product;
        private int quantity;
    }
}
