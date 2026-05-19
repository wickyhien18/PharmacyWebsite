package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "payments")

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
 * Database Entity for Payments.
 * This class is used to map data and handle basic structure.
 */
public class Payments {

    public enum PaymentMethod { COD, VNPAY, MOMO }
    public enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    // Specifies the mapped column for a persistent property or field.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Transaction code from VNPay/Momo — null if COD
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "transaction_code", length = 255)
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    //1 - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    // Specifies a single-valued association to another entity that has one-to-one multiplicity.
    @OneToOne(fetch = FetchType.LAZY)

    //Foreign Key
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // The payment link expires after 15 minutes — a new link must be created
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Save the returned payload VNPay — debug disputes with banks
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "raw_callback", columnDefinition = "JSON")
    private String rawCallback;

    // Count the number of times the user tries to pay again
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "attempt_count")
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private Integer attemptCount = 0;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    /**
     * On create.
     */
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    /**
     * On update.
     */
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
