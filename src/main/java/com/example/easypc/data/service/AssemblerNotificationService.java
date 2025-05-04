package com.example.easypc.data.service;

import com.example.easypc.data.entity.Notification;
import com.example.easypc.data.entity.Order;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.NotificationRepository;
import com.example.easypc.data.repository.OrderRepository;
import com.example.easypc.data.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AssemblerNotificationService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    public void createNotification(String message, Integer orderId, Authentication authentication, List<Integer> replacementProductUrlIds) {
        User assembler = userRepository.findByphone(authentication.getName());
        Order order = orderRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        User user = order.getUser();

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setOrder(order);
        notification.setUser(user);
        notification.setAssembler(assembler);
        notification.setAnswered(false);

        notification.setReplacementProductUrlIds(replacementProductUrlIds);

        notificationRepository.save(notification);
    }
}