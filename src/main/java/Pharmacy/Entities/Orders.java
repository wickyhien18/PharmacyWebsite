package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.orm.jpa.persistenceunit.SpringPersistenceUnitInfo;

import java.time.LocalDateTime;
import java.util.List;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "orders")

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
public class Orders {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "order_id")
    private int order_id;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private Users users;

    private float total_price;
    private String status;

    //Default value is right now
    @CreationTimestamp
    private LocalDateTime created_at;

    //1 - N Relationship
    //Mapped by another entities
    @OneToMany(mappedBy = "orders")

    //Avoid infinite loop
    @JsonIgnore
    private List<OrderItems> orderItems;

    //1- 1 Relationship
    @OneToOne(mappedBy = "orders")
    @JsonIgnore
    private Payments payments;
}
