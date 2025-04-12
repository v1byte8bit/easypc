package com.example.easypc.data.controller.user_profile;

import com.example.easypc.data.controller.ProfileController;
import com.example.easypc.data.service.UserDetailsServiceImpl;
import com.example.easypc.data.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class ProfileUserController extends ProfileController {

    public ProfileUserController(UserDetailsServiceImpl userServiceImpl, UserService userService) {
        super(userServiceImpl, userService);
    }

    @GetMapping("/profile")
    public String showUserProfilePage() {
        return "user_info";
    }

}