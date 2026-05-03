package Pharmacy.Services;

import Pharmacy.Entities.Roles;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Roles> getAll() {
        return roleRepository.findAll();
    }

    public Optional<Roles> findByRoleName(String name) {
        return roleRepository.findByRoleName(name);
    }

    public Roles insert(Roles roles) {
        return roleRepository.save(roles);
    }

    public Roles update(Long id, Roles roles) {
        Roles role1 = roleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", id));
        role1.setRoleName(roles.getRoleName());
        return roleRepository.save(role1);
    }

    public void delete(Integer id) {
        roleRepository.deleteById(Long.valueOf(id));
    }
}
