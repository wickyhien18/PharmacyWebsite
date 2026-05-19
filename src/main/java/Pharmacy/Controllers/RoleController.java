package Pharmacy.Controllers;

import Pharmacy.Entities.Roles;
import Pharmacy.Repositories.RoleRepository;
import Pharmacy.Services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Indicates that this class is a REST controller handling HTTP requests.
@RestController
// Maps HTTP requests to the controller or handler method.
@RequestMapping("/roles")
@Tag(name = "Roles API", description = "Role management")
/**
 * Class RoleController.
 * Provides functionality and data modeling for RoleController.
 */
public class RoleController {

    // Injects the required dependency automatically via Spring DI.
    @Autowired
    private RoleService roleService;

    // Maps HTTP GET requests to this handler method.
    @GetMapping
    @Operation(summary = "Get the list of roles")
    /**
     * Retrieves all roles.
     *
     * @return the List<Roles> result
     */
    public List<Roles> getAllRoles() {
        return roleService.getAll();
    }

}
