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
@Table(name = "medicines")

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
public class Medicines {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "medicine_id")
    private int medicineId;

    @Column(nullable = false, name = "medicine_name")
    private String medicineName;

    @Column(name = "medicine_image")
    private String medicineImage;
    private String description;
    private float price;
    private int quantity;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "manufacturerId")
    private Manufacturers manufacturers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Categories categories;

    //Default value is right now
    @CreationTimestamp
    private LocalDateTime created_at;

    //1 - N Relationship
    //Mapped by another Entities
    @OneToMany(mappedBy = "medicines")

    //Avoid infinite loop
    @JsonIgnore
    private List<CartItems> cartItems;

    @OneToMany(mappedBy = "medicines")
    @JsonIgnore
    private List<OrderItems> orderItems;
}
