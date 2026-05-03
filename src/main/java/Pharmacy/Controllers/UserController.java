package Pharmacy.Controllers;

import Pharmacy.Entities.Users;
import Pharmacy.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users API", description = "Quản lý danh sách người dùng")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary= "Lấy danh sách người dùng")
    public List<Users> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin người dùng đăng nhập dựa vào JWT token")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }
}
