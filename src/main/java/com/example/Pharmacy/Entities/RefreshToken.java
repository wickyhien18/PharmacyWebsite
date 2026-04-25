package com.example.Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "userId")
    private Users users;

    private LocalDateTime expire_at;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expire_at);
    }
}
