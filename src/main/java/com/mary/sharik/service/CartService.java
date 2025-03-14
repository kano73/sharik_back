package com.mary.sharik.service;

import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.dto.request.ActionWithCartDTO;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.OrdersHistory;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.entity.Product;
import com.mary.sharik.model.enums.OrderStatusEnum;
import com.mary.sharik.repository.OrdersHistoryRepository;
import com.mary.sharik.repository.MyUserRepository;
import com.mary.sharik.repository.ProductRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartService {

    private final OrdersHistoryRepository ordersHistoryRepository;
    private final MyUserRepository myUserRepository;
    private final AuthenticatedMyUserService authenticatedMyUserService;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, ProductAndQuantity> redisTemplate;

    @Value("${page.size.history}")
    private Integer PAGE_SIZE;

//    redis

    private static final String CART_KEY_PREFIX = "cart:";

    public void addToCart(ActionWithCartDTO dto) {
        changeAmount(dto, 1);
    }
    public void reduceAmountOrDelete(ActionWithCartDTO dto) {
        changeAmount(dto, -1);
    }


    public List<ProductAndQuantity> getCart(){
        return getCartByUserId(authenticatedMyUserService.getCurrentUserAuthenticated().getId());
    }
    public List<ProductAndQuantity> getCartByUserId(String userId) {
        String cartKey = CART_KEY_PREFIX + userId;
        return redisTemplate.opsForList().range(cartKey, 0, -1);
    }



    private void changeAmount(ActionWithCartDTO dto, int sigh) {
        if(sigh != 1 && sigh != -1){
            throw new RuntimeException("sigh can be only 1 or -1");
        }

        String cartKey = CART_KEY_PREFIX + authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        List<ProductAndQuantity> cart = redisTemplate.opsForList().range(cartKey, 0, -1);

        if (cart == null || cart.isEmpty()) {
            addToCart(dto, cartKey);
            return;
        }

        boolean isChanged = false;
        for (int i = 0; i < cart.size(); i++) {
            ProductAndQuantity item = cart.get(i);
            if (item.getProduct().getId().equals(dto.getProductId())) {
                isChanged = true;
                item.setQuantity(item.getQuantity() + (sigh*dto.getQuantity()));

                if (item.getQuantity() <= 0) {
                    redisTemplate.opsForList().remove(cartKey, 1, item);
                } else {
                    redisTemplate.opsForList().set(cartKey, i, item);
                }

                redisTemplate.expire(cartKey, 1, TimeUnit.HOURS);
                break;
            }
        }
        if(!isChanged){
            addToCart(dto, cartKey);
        }
    }

    private void addToCart(ActionWithCartDTO dto, String cartKey){
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->
                new NoDataFoundException(String.format("Product %s not found", dto.getProductId())));

        ProductAndQuantity paq = new ProductAndQuantity();
        paq.setProduct(product);
        paq.setQuantity(dto.getQuantity());
        redisTemplate.opsForList().rightPush(cartKey, paq);
        redisTemplate.expire(cartKey, 1, TimeUnit.HOURS);
    }

//  between

    public void makeOrder(String customAddress) {
        moveToHistoryAndSetStatus(OrderStatusEnum.CREATED, customAddress);
    }
    public void emptyCart() {
        moveToHistoryAndSetStatus(OrderStatusEnum.CANCELLED, "");
    }

    private void moveToHistoryAndSetStatus(OrderStatusEnum status, String customAddress) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        OrdersHistory ordersHistory = getHistory(user.getId());

        List<ProductAndQuantity> products = redisTemplate.opsForList().range(CART_KEY_PREFIX + user.getId(), 0, -1);

        if(products == null || products.isEmpty()) {
            return;
        }

        List<OrdersHistory.CartItem> cartItems = products.stream()
                .map(product ->{
                    OrdersHistory.CartItem item = new OrdersHistory.CartItem();
                    item.setProductId(product.getProduct().getId());
                    item.setQuantity(product.getQuantity());
                    return item;
                }).toList();

        OrdersHistory.Order order = new OrdersHistory.Order();
        order.setItems(cartItems);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(customAddress == null ? user.getAddress() : customAddress);

        ordersHistory.getOrders().add(order);

        redisTemplate.delete(CART_KEY_PREFIX + user.getId());
        ordersHistoryRepository.save(ordersHistory);
    }

//    mongo

    public List<OrdersHistory.Order> getOrdersHistory() {
        String userId = authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        return getOrdersHistoryByUserId(userId);
    }
    public List<OrdersHistory.Order> getOrdersHistoryByUserId(String userId) {
        return getHistory(userId).getOrders();
    }

    private OrdersHistory getHistory(String userId) {
        if(myUserRepository.findById(userId).isEmpty()) {
            throw new NoDataFoundException("No user found with id " + userId);
        }
        return ordersHistoryRepository.findByUserId(userId)
                .orElseGet(() -> {
                    OrdersHistory newOrderHistory = new OrdersHistory();
                    newOrderHistory.setUserId(userId);
                    return ordersHistoryRepository.save(newOrderHistory);
                });
    }

    public List<OrdersHistory> getWholeHistory(@NotBlank @Min(1) Integer page) {
        return ordersHistoryRepository.findAll(PageRequest.of(page-1, PAGE_SIZE )).getContent();
    }
}
