package Pharmacy.Config;

import Pharmacy.Entities.Users;
import Pharmacy.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
/**
 * Class CustomUserDetailService.
 * Provides functionality and data modeling for CustomUserDetailService.
 */
public class CustomUserDetailService implements UserDetailsService {
    //Implement UserDetailsService - core interface in Spring Security
    //It's searching user's information in Database based on UserName
    //It return UserDetail that automatically set into SecurityContextHolder,
    //          enable features such as @PreAuthorize to work smoothly

    private final UserRepository userRepository;

    @Override
    /**
     * Load user by username.
     *
     * @param email the email
     * @return the UserDetails result
     */
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + email));

        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                //Return a list with one element
                //SimpleGrantAuthority("ROLE_ADMIN") -> authority: ROLE_ADMIN -> hasRole("ADMIN") -> pass
                return Collections.singletonList(new SimpleGrantedAuthority(users.getRoles().getRoleName()));
            }

            @Override
    /**
     * Retrieves password.
     *
     * @return the String result
     */
            public String getPassword() {
                return users.getPassword();
                //Spring Security automatically compare with password in database with password user receive
                //If password isn't equal to password in database, Spring Security will throw BadCredentialsException

            }

            @Override
    /**
     * Retrieves username.
     *
     * @return the String result
     */
            public String getUsername() {
                return users.getEmail();  // Dùng email làm username
            }

            @Override
    /**
     * Checks if credentials non expired.
     *
     * @return the boolean result
     */
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
    /**
     * Checks if enabled.
     *
     * @return the boolean result
     */
            public boolean isEnabled() {
                return users.getIsActive() != null && users.getIsActive();
                //If user's account isn't active, Spring Security will throw DisabledException
            }

            // Thêm method để lấy Users entity
    /**
     * Retrieves user.
     *
     * @return the Users result
     */
            public Users getUser() {
                return users;
            }
        };
    }
}