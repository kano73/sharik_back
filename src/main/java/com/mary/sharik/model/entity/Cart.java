package com.mary.sharik.model.entity;

import com.mary.sharik.model.enums.OrderStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {

    @Id
    private String userId;
    private ActiveCart activeCart;
    private List<Order> orderHistory;

    @Data
    public static class ActiveCart {
        private List<CartItem> items;
        private LocalDateTime createdAt;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Order extends ActiveCart {
        private String orderId;
        private OrderStatusEnum status;
        private String deliveryAddress;
    }

    @Data
    public static class CartItem {
        private String productId;
        private int quantity;
    }
}
