package com.mary.sharik.model.entity;

import com.mary.sharik.model.enums.OrderStatusEnum;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Document(collection = "orderHistory")
public class OrdersHistory {

    @Id
    private String userId;
    private List<Order> orders = new ArrayList<>();

    @Data
    public static class Order{
        private List<CartItem> items;
        private LocalDateTime createdAt;
        private String orderId;
        private OrderStatusEnum status;
        private String deliveryAddress;
    }

    @Data
    public static class CartItem {
        private String productId;
        private int quantity;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            CartItem cartItem = (CartItem) o;
            return Objects.equals(productId, cartItem.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(productId);
        }
    }
}
