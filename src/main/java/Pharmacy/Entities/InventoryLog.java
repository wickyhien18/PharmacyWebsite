package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//Mark this class is Entity in database
@Entity

//Specify table in database
@Table(name = "inventory_logs")

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
public class InventoryLog {

    public enum ChangeType { IMPORT, EXPORT, ADJUST }

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicines medicine;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    // order_id nếu là bán hàng, null nếu nhập kho thủ công
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}