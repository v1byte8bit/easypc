package com.example.easypc.data.service;

import com.example.easypc.data.dto.NotificationDto;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.NotificationRepository;
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

    public List<NotificationDto> getNotificationsForCurrentUser() {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByphone(phone);

        return notificationRepository.findByUserId(user.getId())
                .stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getMessage(),
                        n.getReplacementProductUrlIds()
                ))
                .collect(Collectors.toList());
    }
}