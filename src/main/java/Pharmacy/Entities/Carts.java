package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "carts")

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
public class Carts {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "cart_id")
    private int cart_id;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @OneToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "userId")
    private Users users;

    //Default value is right now
    @CreationTimestamp
    private LocalDateTime created_at;

    //1 - N Relationship
    //Mapped by another Entities
    @OneToMany(mappedBy = "carts")

    //Avoid infinite loop
    @JsonIgnore
    private List<CartItems> cartItems;

}
