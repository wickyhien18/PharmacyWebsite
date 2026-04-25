package com.example.Pharmacy.Services;

import com.example.Pharmacy.DTO.UserProfile;
import com.example.Pharmacy.Entities.Users;
import com.example.Pharmacy.Repositories.UserRepository;
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

    public Users findByIdDetail(Integer id) {
        return userRepository.findByIdDetail(id);
    }

    public  boolean existByUserName(String name) {
        return userRepository.existsByUserName(name);
    }

    public Optional<Users> findByUserName(String name) {
        return userRepository.findByUserName(name);
    }

    public UserProfile getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println(auth);

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String username = auth.getName();

        System.out.println(username);

        Users users = userRepository.findByUserName(username).orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        UserProfile userProfile = UserProfile.builder()
                .userName(users.getUserName())
                .roleName(users.getRoles().getRoleName()).build();
        return userProfile;
    }

    public Users insert(Users users) {
        return userRepository.save(users);
    }

    public Users update(Integer id, Users users) {
        Users users1 = userRepository.findByIdDetail(id);
        users1.setUserName(users.getUserName());
        users1.setPassword(users.getPassword());
        users1.setRoles(users.getRoles());
        return userRepository.save(users1);
    }

    public void delete(Integer id) {
        userRepository.deleteById(Long.valueOf(id));
    }
}
