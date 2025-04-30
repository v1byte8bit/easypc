package com.example.easypc.data.service;

import com.example.easypc.data.dto.CartProductData;
import com.example.easypc.data.entity.*;
import com.example.easypc.data.repository.CartRepository;
import com.example.easypc.data.repository.SourceRepository;
import com.example.easypc.data.repository.UserRepository;
import com.example.easypc.parse.ProductData;
import com.example.easypc.parse.ProductParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final SourceRepository sourceRepository;
    private final UserRepository userRepository;

    @Autowired
    private ProductParserService productParserService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhone(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void addToCart(Integer sourceId) {
        User user = getCurrentUser();
        Source source = sourceRepository.findById(Long.valueOf(sourceId))
                .orElseThrow(() -> new RuntimeException("Source not found"));

        Optional<Cart> existingCartItem = cartRepository.findByUserAndSource(user, source);

        if (existingCartItem.isPresent()) {
            Cart cart = existingCartItem.get();
            cart.setQuantity(cart.getQuantity() + 1);
            cartRepository.save(cart);
        } else {
            Cart newCartItem = new Cart();
            newCartItem.setUser(user);
            newCartItem.setSource(source);
            newCartItem.setQuantity(1);
            cartRepository.save(newCartItem);
        }
    }

    //Получение суммы товаров в корзине
    public BigDecimal getCartTotal(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Set<Long> sourceIds = cartItems.stream()
                .map(cartItem -> cartItem.getSource().getId().longValue())
                .collect(Collectors.toSet());
        Map<Long, Source> sources = sourceRepository.findAllById(sourceIds).stream()
                .collect(Collectors.toMap(source -> (long) source.getId(), Function.identity()));
        Map<Long, ProductData> productDataMap = sources.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        productParserService::parseSingleProduct
                ));

        return cartItems.stream()
                .map(item -> {
                    ProductData productData = productDataMap.get(item.getSource().getId().longValue());

                    if (productData == null || productData.getPrice() == null) {
                        return BigDecimal.ZERO;
                    }

                    try {
                        String cleanPrice = productData.getPrice().replaceAll("[^\\d.]", "");
                        return new BigDecimal(cleanPrice).multiply(BigDecimal.valueOf(item.getQuantity()));
                    } catch (NumberFormatException e) {

                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //Получение товаров в корзине
    public List<CartProductData> getCartItems(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        List<Long> sourceIds = cartItems.stream()
                .map(cartItem -> cartItem.getSource().getId().longValue())
                .collect(Collectors.toList());


        Map<Long, Source> sources = sourceRepository.findAllById(sourceIds)
                .stream()
                .collect(Collectors.toMap(source -> source.getId().longValue(), source -> source));

        List<CompletableFuture<CartProductData>> futures = cartItems.stream()
                .map(cartItem -> CompletableFuture.supplyAsync(() -> {
                    Source source = sources.get(cartItem.getSource().getId().longValue());
                    if (source != null) {
                        ProductData productData = productParserService.parseSingleProduct(source.getId().longValue());
                        if (productData != null) {
                            return new CartProductData(
                                    productData.getName(),
                                    productData.getPrice(),
                                    productData.getCategory(),
                                    productData.getImg(),
                                    productData.getUrlId(),
                                    productData.getCharacteristics(),
                                    cartItem.getQuantity()
                            );
                        }
                    }
                    return null;
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //Метод для изменения количества в корзине с помощью +/-
    @Transactional
    public ResponseEntity<?> updateCartQuantity(Long userId, Long sourceId, int quantity) {
        Optional<Cart> optionalCartItem = cartRepository.findByUserIdAndSourceId(userId, sourceId);
        Cart cartItem = optionalCartItem.get();
        cartItem.setQuantity(quantity);
        cartRepository.saveAndFlush(cartItem);
        return ResponseEntity.ok("Количество товара обновлено");
    }

    @Transactional
    public void removeFromCart(Long sourceId, Long userId) {
        cartRepository.deleteByUserIdAndSourceId(userId, sourceId);
    }

    @Transactional
    public void clearCart(Long userId){
        cartRepository.deleteByUserId(userId);
    }
}