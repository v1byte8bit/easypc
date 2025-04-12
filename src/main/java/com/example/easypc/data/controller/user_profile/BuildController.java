package com.example.easypc.data.controller.user_profile;

import com.example.easypc.data.dto.BuildDto;
import com.example.easypc.data.request.BuildSaveRequest;
import com.example.easypc.data.entity.Build;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.service.BuildService;
import com.example.easypc.data.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class BuildController {

    @Autowired
    private BuildService buildService;

    @Autowired
    private UserService userService;

    @GetMapping("/builds")
    public String showBuildsPage() {
        return "user_builds";
    }

    @PostMapping("/cart/save_build")
    public ResponseEntity<Void> saveCartToBuild(@RequestBody BuildSaveRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        Long userId = userService.getUserIdByUsername(phone);

        buildService.saveCartToBuild(userId, request.getBuildName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/builds")
    public ResponseEntity<List<BuildDto>> getUserBuilds(Authentication authentication) {
        String phone = authentication.getName();
        Long userId = userService.getUserIdByUsername(phone);
        return ResponseEntity.ok(buildService.getUserBuilds(userId));
    }

    @DeleteMapping("/builds/{buildId}")
    public ResponseEntity<?> deleteBuild(@PathVariable Long buildId, Authentication authentication) {
        String phone = authentication.getName();
        Long userId = userService.getUserIdByUsername(phone);
        Build build = buildService.findByIdAndUser(buildId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сборка не найдена"));

        buildService.deleteBuild(build);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/builds/{buildId}/tocart")
    public ResponseEntity<?> addBuildToCart(@PathVariable Long buildId, Authentication authentication) {
        String phone = authentication.getName();
        Long userId = userService.getUserIdByUsername(phone);
        User user = userService.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        Build build = buildService.findByIdAndUser(buildId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сборка не найдена"));

        buildService.addBuildItemsToCart(user, build.getItems());

        return ResponseEntity.ok("Товары добавлены в корзину");
    }
}
