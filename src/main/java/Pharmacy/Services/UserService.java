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

/**
 * Service class for managing User entities.
 * Contains business logic for user-related operations including CRUD.
 */
// Indicates that this class provides business logic and acts as a service.
@Service
public class UserService {

    // Injects the required dependency automatically via Spring DI.
    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves all users from the database.
     *
     * @return A list of all Users entities.
     */
    public List<Users>  getAll() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by their username.
     *
     * @param name The username to search for.
     * @return An Optional containing the user if found, or empty otherwise.
     */
    public Optional<Users> findByUserName(String name) {
        return userRepository.findByUserName(name);
    }

    /**
     * Inserts a new user into the database.
     *
     * @param users The user entity to be saved.
     * @return The saved user entity.
     */
    public Users insert(Users users) {
        return userRepository.save(users);
    }

    /**
     * Updates an existing user with new details.
     *
     * @param id The ID of the user to update.
     * @param users The user object containing updated information.
     * @return The updated user entity.
     * @throws ResourceNotFoundException if the user is not found.
     */
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

    /**
     * Deletes a user from the database by ID.
     *
     * @param id The ID of the user to delete.
     */
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
