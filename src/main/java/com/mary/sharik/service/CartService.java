package com.mary.sharik.service;

import com.mary.sharik.config.security.AuthenticatedMyUserService;
import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.dto.ActionWithCartDTO;
import com.mary.sharik.model.entity.Cart;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.OrderStatusEnum;
import com.mary.sharik.repository.CartRepository;
import com.mary.sharik.repository.MyUserRepository;
import com.mary.sharik.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public Cart.ActiveCart getActiveCart() {
        String userId = authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        return getActiveCartByUserId(userId);
    }

    public Cart.ActiveCart getActiveCartByUserId(String userId) {
        return getOrCreateCart(userId).getActiveCart();
    }

    public List<Cart.Order> getHistory() {
        String userId = authenticatedMyUserService.getCurrentUserAuthenticated().getId();
        return getHistoryByUserId(userId);
    }

    public List<Cart.Order> getHistoryByUserId(String userId) {
        return getOrCreateCart(userId).getOrderHistory();
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

    public void makeOrder(String customAddress) {
        moveToHistoryAndSetStatus(OrderStatusEnum.CREATED, customAddress);
    }

    public void emptyCart() {
        moveToHistoryAndSetStatus(OrderStatusEnum.CANCELLED, "");
    }

    private void moveToHistoryAndSetStatus(OrderStatusEnum status, String customAddress) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        Cart cart = getOrCreateCart(user.getId());

        Cart.Order order = new Cart.Order();
        order.setItems(cart.getActiveCart().getItems());
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(customAddress == null ? user.getAddress() : customAddress);

        cart.getOrderHistory().add(order);
        cart.setActiveCart(new Cart.ActiveCart());
        cartRepository.save(cart);
    }

    public void reduceAmountOrDelete(ActionWithCartDTO dto) {
        MyUser user = authenticatedMyUserService.getCurrentUserAuthenticated();
        Cart cart = getOrCreateCart(user.getId());

        List<Cart.CartItem> items = cart.getActiveCart().getItems();
        items.stream().filter(item->item.getProductId().equals(dto.getProductId()))
                .findFirst().ifPresent(item -> {
                    item.setQuantity(item.getQuantity() - dto.getQuantity());
                    if(item.getQuantity() <= 0) {
                        items.remove(item);
                    }
                });

        cartRepository.save(cart);
    }
}
