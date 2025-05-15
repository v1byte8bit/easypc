package com.example.easypc.data.service;

import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Long getUserIdByUsername(String phone) {
        return userRepository.findByPhone(phone)
                .map(User::getId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}