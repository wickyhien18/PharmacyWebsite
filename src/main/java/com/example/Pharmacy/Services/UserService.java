package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Users;
import com.example.Pharmacy.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<Users>  getAll() {
        return userRepository.findAll();
    }

    public  boolean existByUserName(String name) {
        return userRepository.existsByUserName(name);
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
