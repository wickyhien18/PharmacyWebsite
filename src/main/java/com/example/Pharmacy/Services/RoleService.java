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
}
