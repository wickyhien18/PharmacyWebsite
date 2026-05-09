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
public class CustomUserDetailService implements UserDetailsService {
    //Implement UserDetailsService - core interface in Spring Security
    //It's searching user's information in Database based on UserName
    //It return UserDetail that automatically set into SecurityContextHolder,
    //          enable features such as @PreAuthorize to work smoothly

    private final UserRepository userRepository;

    @Override
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
            public String getPassword() {
                return users.getPassword();
                //Spring Security automatically compare with password in database with password user receive
                //If password isn't equal to password in database, Spring Security will throw BadCredentialsException

            }

            @Override
            public String getUsername() {
                return users.getEmail();  // Dùng email làm username
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return users.getIsActive() != null && users.getIsActive();
                //If user's account isn't active, Spring Security will throw DisabledException
            }

            // Thêm method để lấy Users entity
            public Users getUser() {
                return users;
            }
        };
    }
}