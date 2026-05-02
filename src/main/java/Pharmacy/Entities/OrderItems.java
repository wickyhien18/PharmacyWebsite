package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "order_item")

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
public class OrderItems {

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(nullable = false)
    private Integer quantity;

    // Snapshot giá lúc đặt — giá sau có thể thay đổi
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @ManyToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "order_id")
    private Orders orders;

    //1 - N Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicines medicines;
}
