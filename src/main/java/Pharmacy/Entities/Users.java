package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import Pharmacy.Entities.Roles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "users")

//Create object easily
// ClassName.builder().atribute1().attribute2.build()
@Builder

//Create constructor no args
@NoArgsConstructor

//Create constructor with all args
@AllArgsConstructor

//Generate Getter method for all attributes
@Getter
//Generate Setter method for all attributes
@Setter
public class Users implements UserDetails {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, name = "user_name", length = 100)
    private String userName;

    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "create_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "delete_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "role_id")
    private Roles roles;

    //1 - 1 Relationship
    //Mapped by another entities
    @OneToOne(mappedBy = "users")

    //Avoid infinite loop
    @JsonIgnore
    private Carts carts;

    //1 - N Relationship
    @OneToMany(mappedBy = "users")
    @JsonIgnore
    private List<Orders> orders;

    @Override
    public String getUsername() { return email; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) return List.of();
        // roleName has been prefix "ROLE_" to Spring Security
        return List.of(new SimpleGrantedAuthority(roles.getRoleName()));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return deletedAt == null; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return Boolean.TRUE.equals(isActive) && deletedAt == null; }
}
