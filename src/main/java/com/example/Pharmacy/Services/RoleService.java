package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Roles;
import com.example.Pharmacy.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Roles> getAll() {
        return roleRepository.findAll();
    }

    public Roles insert(Roles roles) {
        return roleRepository.save(roles);
    }

    public Roles update(Integer id, Roles roles) {
        Roles role1 = roleRepository.findByIdDetail(id);
        role1.setRolename(roles.getRolename());
        role1.setDescription(roles.getDescription());
        return roleRepository.save(role1);
    }

    public void delete(Integer id) {
        roleRepository.deleteById(Long.valueOf(id));
    }
}
