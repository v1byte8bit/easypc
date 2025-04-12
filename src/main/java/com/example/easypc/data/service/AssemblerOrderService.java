package com.example.easypc.data.service;

import com.example.easypc.data.dto.OrderDto;
import com.example.easypc.data.dto.OrderItemDto;
import com.example.easypc.data.entity.Order;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.OrderRepository;
import com.example.easypc.data.repository.UserRepository;
import com.example.easypc.data.parse.ProductData;
import com.example.easypc.data.parse.ProductParserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AssemblerOrderService {
    private final OrderRepository orderRepository;
    private final ProductParserService productParserService;
    private final UserRepository userRepository;

    public List<OrderDto> getCreatedOrders() {
        List<Order> orders = orderRepository.findByStatus("Создан");

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
    public void takeOrder(Long orderId, Authentication authentication) {
        User builder = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        // Проверяем статус заказа
        if (!"Создан".equals(order.getStatus())) {
            throw new IllegalArgumentException("Этот заказ уже в работе");
        }

        // Обновляем заказ
        order.setBuilder(builder);
        order.setStatus("На сборке");
        orderRepository.save(order);
    }
}
