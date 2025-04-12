package com.example.easypc.data.controller.assembler_profile;


import com.example.easypc.data.controller.ProfileController;
import com.example.easypc.data.service.UserDetailsServiceImpl;
import com.example.easypc.data.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/assembler")
public class ProfileAssemblerController extends ProfileController {
    public ProfileAssemblerController(UserDetailsServiceImpl userServiceImpl, UserService userService) {
        super(userServiceImpl, userService);
    }

    @GetMapping("/profile")
    public String showAssemblerProfilePage() {
        return "assembler_info";
    }
}
