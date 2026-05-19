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
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "users")

//Create object easily
// ClassName.builder().atribute1().attribute2.build()
// Produces complex builder APIs for this class via Lombok.
@Builder

//Create constructor no args
// Generates a no-argument constructor via Lombok.
@NoArgsConstructor

//Create constructor with all args
// Generates an all-arguments constructor via Lombok.
@AllArgsConstructor

//Generate Getter method for all attributes
@Getter
//Generate Setter method for all attributes
@Setter
/**
 * Database Entity for Users.
 * This class is used to map data and handle basic structure.
 */
public class Users implements UserDetails {

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "user_id")
    private Long userId;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "user_name", nullable = false, unique = true, length = 100)
    private String userName;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, length = 255)
    private String password;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @CreationTimestamp
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreationTimestamp
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "is_active")
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private Boolean isActive = true;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.EAGER)

    //Foreign Key
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "role_id")
    private Roles roles;

    @PrePersist
    /**
     * On create.
     */
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }

    @PreUpdate
    /**
     * On update.
     */
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    /**
     * Retrieves username.
     *
     * @return the String result
     */
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
