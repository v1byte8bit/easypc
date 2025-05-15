package com.example.easypc.data.service;

import com.example.easypc.data.dto.NotificationDto;
import com.example.easypc.data.entity.Order;
import com.example.easypc.data.entity.OrderItem;
import com.example.easypc.data.entity.Source;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.NotificationRepository;
import com.example.easypc.data.repository.OrderRepository;
import com.example.easypc.data.repository.SourceRepository;
import com.example.easypc.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SourceRepository sourceRepository;

    public List<NotificationDto> getNotificationsForCurrentUser() {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByphone(phone);

        return notificationRepository.findByUserId(user.getId())
                .stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getMessage(),
                        n.getOrder() != null ? n.getOrder().getIdOrder() : null,
                        n.getReplacementProductUrlIds()
                ))
                .collect(Collectors.toList());
    }

    public void replaceProductInOrder(Long orderId, Integer replacementUrlId, String phone) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        Source replacementSource = (Source) sourceRepository.findById(replacementUrlId)
                .orElseThrow(() -> new RuntimeException("Источник замены не найден"));

        String replacementCategory = replacementSource.getCategory();

        for (OrderItem item : order.getItems()) {
            if (item.getUrl().getCategory().equals(replacementCategory)) {
                item.setUrl(replacementSource);
                break;
            }
        }
        orderRepository.save(order);
    }
}