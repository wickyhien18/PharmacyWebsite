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

/**
 * REST Controller for managing User operations.
 * Exposes API endpoints for retrieving and managing user data within the system.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users API", description = "Users Management")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Retrieves a list of all users.
     *
     * @return A list of Users entities containing user details.
     */
    @GetMapping
    @Operation(summary= "Get user list")
    public List<Users> getAllUsers() {
        return userService.getAll();
    }
}
