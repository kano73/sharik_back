package com.mary.sharik.service;

import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.entity.Cart;
import com.mary.sharik.model.enums.OrderStatusEnum;
import com.mary.sharik.repository.CartRepository;
import com.mary.sharik.repository.MyUserRepository;
import com.mary.sharik.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MyUserRepository myUserRepository;
    private final AuthenticatedMyUserService authenticatedMyUserService;
    private final ProductRepository productRepository;

    public Cart getOrCreateCart(String userId) {
        if(myUserRepository.findById(userId).isEmpty()) {
            throw new NoDataFoundException("No user found with id " + userId);
        }
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setActiveCart(new Cart.ActiveCart());
                    newCart.getActiveCart().setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });
    }

    public void addItemToCart(String productId, int quantity) {
        String userId = authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        productRepository.findById(productId).orElseThrow(()->
                new NoDataFoundException("No product found")
        );

        Cart cart = getOrCreateCart(userId);

        Cart.CartItem cartItem = new Cart.CartItem();
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);

        cart.getActiveCart().getItems().add(cartItem);
        cartRepository.save(cart);
    }

    public void completeOrder(String address) {
        String userId = authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        Cart cart = getOrCreateCart(userId);

        Cart.Order order = new Cart.Order();
        order.setItems(cart.getActiveCart().getItems());
        order.setStatus(OrderStatusEnum.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(address);

        cart.getOrderHistory().add(order);
        cart.setActiveCart(new Cart.ActiveCart());
        cartRepository.save(cart);
    }
}
