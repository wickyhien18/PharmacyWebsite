package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Users;
import com.example.Pharmacy.Repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users API", description = "Quản lý danh sách người dùng")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @Operation(summary= "Lấy danh sách người dùng")
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }
}
