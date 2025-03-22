package com.mary.sharik.service;

import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.exceptions.ValidationFailedException;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
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
        changeAmount(dto);
    }

    public void resetAmountOrDelete(ActionWithCartDTO dto) {
        String cartKey = CART_KEY_PREFIX + authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        List<ProductAndQuantity> cart = redisTemplate.opsForList().range(cartKey, 0, -1);

        if (cart == null || cart.isEmpty()) {
            return;
        }

        for (int i = 0; i < cart.size(); i++) {
            ProductAndQuantity item = cart.get(i);
            if (item.getProduct().getId().equals(dto.getProductId())) {
                if (dto.getQuantity() <= 0) {
                    redisTemplate.opsForList().remove(cartKey, 1, item);
                } else {
                    if(dto.getQuantity()>item.getProduct().getAmountLeft()) {
                        throw new ValidationFailedException("These is not enough product amount left");
                    }
                    item.setQuantity(dto.getQuantity());
                    redisTemplate.opsForList().set(cartKey, i, item);
                }
                redisTemplate.expire(cartKey, 1, TimeUnit.HOURS);
                break;
            }
        }
    }

    public List<ProductAndQuantity> getCart(){
        return getCartByUserId(authenticatedMyUserService.getCurrentUserAuthenticated().getId());
    }
    public List<ProductAndQuantity> getCartByUserId(String userId) {
        String cartKey = CART_KEY_PREFIX + userId;
        return redisTemplate.opsForList().range(cartKey, 0, -1);
    }

    private void changeAmount(ActionWithCartDTO dto) {
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


                if (item.getQuantity() <= 0) {
                    redisTemplate.opsForList().remove(cartKey, 1, item);
                } else {
                    item.setQuantity(item.getQuantity() + (dto.getQuantity()));
                    if(item.getProduct().getAmountLeft()<item.getQuantity()) {
                        throw new ValidationFailedException("These is not enough product amount left");
                    }
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
        if(product.getAmountLeft()<dto.getQuantity()){
            throw new ValidationFailedException("These is not enough product amount left");
        }

        ProductAndQuantity paq = new ProductAndQuantity();
        paq.setProduct(product);
        paq.setQuantity(dto.getQuantity());
        redisTemplate.opsForList().rightPush(cartKey, paq);
        redisTemplate.expire(cartKey, 1, TimeUnit.HOURS);
    }

//  between

    @Transactional
    public void makeOrder(String customAddress) {
        moveToHistoryAndSetStatus(OrderStatusEnum.CREATED, customAddress);
    }

    public void emptyCart() {
        moveToHistoryAndSetStatus(OrderStatusEnum.CANCELLED, "");
    }

    private void moveToHistoryAndSetStatus(OrderStatusEnum status, String customAddress) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        OrdersHistory ordersHistory = getHistoryOfUserById(user.getId());

        List<ProductAndQuantity> paq = redisTemplate.opsForList().range(CART_KEY_PREFIX + user.getId(), 0, -1);

        if(paq == null || paq.isEmpty()) {
            return;
        }

        List<OrdersHistory.CartItem> cartItems = paq.stream()
                .map(product ->{
                    OrdersHistory.CartItem item = new OrdersHistory.CartItem();
                    item.setProduct(product.getProduct());
                    item.setQuantity(product.getQuantity());
                    return item;
                }).toList();

        if(status==OrderStatusEnum.CREATED){
            cartItems.forEach(paq1 -> {
                Product prod = productRepository.findById(paq1.getProduct().getId()).orElseThrow(()->
                        new NoDataFoundException("no product found with id:"+ paq1.getProduct().getId())
                );

                if(prod.getAmountLeft()<paq1.getQuantity()){
                    throw new ValidationFailedException("These is not enough product amount left");
                }

                prod.setAmountLeft(prod.getAmountLeft()-paq1.getQuantity());
                productRepository.save(prod);
            });
        }

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

    public OrdersHistory getOrdersHistory() {
        String userId = authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        return getHistoryOfUserById(userId);
    }

    public OrdersHistory getHistoryOfUserById(String userId) {
        if(myUserRepository.findById(userId).isEmpty()) {
            throw new NoDataFoundException("No user found with id " + userId);
        }
        return ordersHistoryRepository.findByUserId(userId)
                .orElseGet(() -> {
                    OrdersHistory newOrderHistory = new OrdersHistory();
                    newOrderHistory.setUserId(userId);
                    return newOrderHistory;
                });
    }

    public List<OrdersHistory> getWholeHistory(@NotBlank @Min(1) Integer page) {
        return ordersHistoryRepository
                .findAll(PageRequest.of(page - 1, PAGE_SIZE))
                .getContent();

    }

//    scheduled task

    @Scheduled(cron = "0 0 * * * *")
    public void simulateStatusModification() {
        System.out.println("simulateStatusModification");
        List<OrderStatusEnum> statusEnumList = Arrays.asList(OrderStatusEnum.values());
        List<OrdersHistory> all = ordersHistoryRepository.findAll();
        all.forEach(ordersHistory -> ordersHistory.getOrders().forEach(order -> {
            OrderStatusEnum status = order.getStatus();
            int index = statusEnumList.indexOf(status);
            if(index != statusEnumList.size() - 3 && status!=OrderStatusEnum.CANCELLED) {
                status = statusEnumList.get(index + 1);
                order.setStatus(status);
            }
        }));
        ordersHistoryRepository.saveAll(all);
    }
}
