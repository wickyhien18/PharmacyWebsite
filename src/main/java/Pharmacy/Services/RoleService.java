package Pharmacy.Services;

import Pharmacy.Entities.Roles;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
/**
 * Class RoleService.
 * Provides functionality and data modeling for RoleService.
 */
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Retrieves all.
     *
     * @return the List<Roles> result
     */
    public List<Roles> getAll() {
        return roleRepository.findAll();
    }

    /**
     * Finds by role name.
     *
     * @param name the name
     * @return the Optional<Roles> result
     */
    public Optional<Roles> findByRoleName(String name) {
        return roleRepository.findByRoleName(name);
    }

    /**
     * Creates a new Insert.
     *
     * @param roles the roles
     * @return the Roles result
     */
    public Roles insert(Roles roles) {
        return roleRepository.save(roles);
    }

    /**
     * Updates an existing .
     *
     * @param id the id
     * @param roles the roles
     * @return the Roles result
     */
    public Roles update(Long id, Roles roles) {
        Roles role1 = roleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", id));
        role1.setRoleName(roles.getRoleName());
        return roleRepository.save(role1);
    }

    /**
     * Deletes .
     *
     * @param id the id
     */
    public void delete(Integer id) {
        roleRepository.deleteById(Long.valueOf(id));
    }
}
