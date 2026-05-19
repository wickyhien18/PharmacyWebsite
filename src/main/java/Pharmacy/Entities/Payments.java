package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
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
/**
 * Database Entity for Payments.
 * This class is used to map data and handle basic structure.
 */
public class Payments {

    public enum PaymentMethod { COD, VNPAY, MOMO }
    public enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Transaction code from VNPay/Momo — null if COD
    @Column(name = "transaction_code", length = 255)
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    //1 - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @OneToOne(fetch = FetchType.LAZY)

    //Foreign Key
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // The payment link expires after 15 minutes — a new link must be created
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Save the returned payload VNPay — debug disputes with banks
    @Column(name = "raw_callback", columnDefinition = "JSON")
    private String rawCallback;

    // Count the number of times the user tries to pay again
    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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
