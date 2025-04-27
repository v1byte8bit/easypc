package com.example.easypc.data.service;

import com.example.easypc.data.dto.OrderDto;
import com.example.easypc.data.dto.OrderItemDto;
import com.example.easypc.data.request.OrderRequest;
import com.example.easypc.data.entity.Order;
import com.example.easypc.data.entity.OrderItem;
import com.example.easypc.data.entity.Source;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.OrderRepository;
import com.example.easypc.data.repository.SourceRepository;
import com.example.easypc.data.repository.UserRepository;
import com.example.easypc.parse.ProductData;
import com.example.easypc.parse.ProductParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final SourceRepository sourceRepository;

    @Autowired
    private ProductParserService productParserService;

    @Transactional
    public Order createOrder(OrderRequest orderRequest, Authentication authentication) {
        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(Double.valueOf(String.valueOf(cartService.getCartTotal(user.getId()))));
        order.setStatus("Создан");
        order.setPhone(orderRequest.getPhone());
        order.setAddress(orderRequest.getAddress());

        List<OrderItem> orderItems = cartService.getCartItems(user.getId()).stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    Source source = sourceRepository.findById(cartItem.getUrlId())
                            .orElseThrow(() -> new RuntimeException("Источник не найден"));
                    orderItem.setUrl(source);
                    orderItem.setQuantity(cartItem.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setItems(orderItems);
        orderRepository.save(order);

        cartService.clearCart(user.getId());
        return order;
    }

    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream().map(order -> {
            List<OrderItemDto> items = order.getItems().stream().map(item -> {
                Long sourceId = Long.valueOf(item.getUrl().getId());
                ProductData productData = productParserService.parseSingleProduct(sourceId);
                return new OrderItemDto(
                        item.getId(),
                        productData.getName(),
                        productData.getPrice(),
                        productData.getImg(),
                        item.getQuantity(),
                        item.getUrl().getSource()
                );
            }).toList();

            return new OrderDto(order.getIdOrder(), order.getStatus(), order.getTotalPrice(), order.getAddress(), order.getPhone(), items);
        }).toList();
    }

    @Transactional
    public boolean cancelOrder(Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            if (!"Создан".equals(order.getStatus())) {
                return false; // Только заказы со статусом "Создан" можно отменить
            }
            order.setStatus("Отменен");
            orderRepository.save(order);
            return true;
        }).orElse(false);
    }
}