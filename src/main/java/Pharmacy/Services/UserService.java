package Pharmacy.Services;

import Pharmacy.Entities.Users;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<Users>  getAll() {
        return userRepository.findAll();
    }

    public Optional<Users> findByUserName(String name) {
        return userRepository.findByUserName(name);
    }

    public Users insert(Users users) {
        return userRepository.save(users);
    }

    public Users update(Long id, Users users) {
        Users users1 = userRepository
                .findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        users1.setUserName(users.getUsername());
        users1.setFullName(users.getFullName());
        users1.setPassword(users.getPassword());
        users1.setEmail(users.getEmail());
        users1.setPhone(users.getPhone());
        users1.setRoles(users.getRoles());
        return userRepository.save(users1);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
