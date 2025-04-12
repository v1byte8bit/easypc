package com.example.easypc.data.service;

import com.example.easypc.data.dto.OrderDto;
import com.example.easypc.data.dto.OrderItemDto;
import com.example.easypc.data.entity.Order;
import com.example.easypc.data.repository.OrderRepository;
import com.example.easypc.data.parse.ProductData;
import com.example.easypc.data.parse.ProductParserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class AssemblerWorkService {
    private final OrderRepository orderRepository;
    private final ProductParserService productParserService;

    //заказы в работе у сборщика
    public List<OrderDto> getAssemblerWork(Long assemblerId) {
        List<Order> orders = orderRepository.findByBuilderId(assemblerId);

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

    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));

        order.setStatus(newStatus);
        orderRepository.save(order);
    }
}
