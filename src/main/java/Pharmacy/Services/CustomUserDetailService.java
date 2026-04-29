package Pharmacy.Services;

import Pharmacy.Entities.Users;
import Pharmacy.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Users user = usersRepository
                .findByUserName(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Not found: " + username));

        return new org.springframework.security.core.userdetails
                .User(
                user.getUserName(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRoles().getRoleName()))
        );
    }
}
