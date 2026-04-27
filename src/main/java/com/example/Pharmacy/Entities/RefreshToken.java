package com.example.Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "refresh_token")

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
public class RefreshToken {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "id")
    private long id;

    private String token;

    //1 - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @OneToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "userId")
    private Users users;

    private LocalDateTime expire_at;

    //Default value is right now
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expire_at);
    }
}
