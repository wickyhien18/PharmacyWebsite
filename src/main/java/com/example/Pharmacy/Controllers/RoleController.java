package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Roles;
import com.example.Pharmacy.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<Roles> getAllRoles() {
        return roleRepository.findAll();
    }

//    @GetMapping("/{id}")
//    public Roles getRoleById(@PathVariable int id) {
//        Optional<Roles> role = roleRepository.findById(id);
//        return  role.orElse(null);
//    }
//
//    @PostMapping("/add")
//    public Roles insert(@RequestBody Roles role) {
//        return roleRepository.save(role);
//    }
//
//    @PutMapping
//    public Roles update(@PathVariable int id, @RequestBody Roles updateRole) {
//        Optional<Roles> role = roleRepository.findById(id);
//        if (role.isPresent()) {
//            Roles exitRole = role.get();
//            exitRole.setRole_name(updateRole.getRole_name());
//            exitRole.setDescription(updateRole.getDescription());
//            return roleRepository.save(exitRole);
//        }
//        return  null;
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable int id) {
//        roleRepository.deleteById(id);
//    }
}
