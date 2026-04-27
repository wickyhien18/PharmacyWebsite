package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "payments")

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
public class Payments {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "payment_id")
    private int payment_id;

    @Column(name = "payment_method")
    private String payment_method;

    private float amount;
    private String status;

    //Default value is right now
    @CreationTimestamp
    private LocalDateTime created_at;

    //1 - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @OneToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "order_id")
    private Orders orders;
}
