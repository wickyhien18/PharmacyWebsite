package Pharmacy.Controllers;

import Pharmacy.Entities.Roles;
import Pharmacy.Repositories.RoleRepository;
import Pharmacy.Services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@Tag(name = "Roles API", description = "Quản lý vai trò")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @Operation(summary = "Lấy danh sách vai trò")
    public List<Roles> getAllRoles() {
        return roleService.getAll();
    }

}
