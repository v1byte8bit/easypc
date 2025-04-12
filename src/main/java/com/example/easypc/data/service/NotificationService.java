package com.example.easypc.data.service;

import com.example.easypc.data.entity.Notification;
import com.example.easypc.data.entity.Order;
import com.example.easypc.data.entity.Source;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.NotificationRepository;
import com.example.easypc.data.repository.OrderRepository;
import com.example.easypc.data.repository.SourceRepository;
import com.example.easypc.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SourceRepository sourceRepository;

    public Notification createNotification(Long userId, Long orderId, String message, Long variant1Id, Long variant2Id) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        Source variant1 = (variant1Id != null) ? sourceRepository.findById(variant1Id).orElse(null) : null;
        Source variant2 = (variant2Id != null) ? sourceRepository.findById(variant2Id).orElse(null) : null;

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setOrder(order);
        notification.setMessage(message);
        notification.setVariant1(variant1);
        notification.setVariant2(variant2);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
}