package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Mark this class is Entity in database
// Specifies that this class is a JPA entity mapped to a database table.
@Entity

//Specify table in database
// Specifies the database table used for mapping this entity.
@Table(name = "orders")

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
 * Database Entity for Orders.
 * This class is used to map data and handle basic structure.
 */
public class Orders {

    public enum OrderStatus  { PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, CANCEL_REQUESTED, RETURN_REQUESTED, RETURNED }
    public enum PaymentStatus { PENDING, PAID, FAILED, REFUNDED }

    //Primary key
    // Specifies the primary key of this entity.
    @Id

    //Id auto_increment
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "order_id")
    private Long orderId;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "user_id")
    private Users users;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "order_code", nullable = false, unique = true, length = 100)
    private String orderCode;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "order_status")
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "payment_status")
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    // Specifies the mapped column for a persistent property or field.
    @Column(columnDefinition = "TEXT")
    private String note;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "cancelled_by", length = 20)
    private String cancelledBy;          // USER / ADMIN

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;      // Reason for cancellation / refund

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //1 - N Relationship
    //Mapped by another entities
    // Specifies a many-valued association with one-to-many multiplicity.
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    // Produces complex builder APIs for this class via Lombok.
    @Builder.Default
    private List<OrderItems> orderItems = new ArrayList<>();

    // ================================================================
    // BUSINESS RULES
    // ================================================================

    // The user self-destructs immediately — only when PENDING
    /**
     * Can user cancel directly.
     *
     * @return the boolean result
     */
    public boolean canUserCancelDirectly() {
        return this.orderStatus == OrderStatus.PENDING;
    }

    // User submits cancellation request — only if CONFIRMED
    // Admin will approve later
    /**
     * Can user request cancel.
     *
     * @return the boolean result
     */
    public boolean canUserRequestCancel() {
        return this.orderStatus == OrderStatus.CONFIRMED;
    }

    // User submits a return request — only when SHIPPING
    /**
     * Can user request return.
     *
     * @return the boolean result
     */
    public boolean canUserRequestReturn() {
        return this.orderStatus == OrderStatus.SHIPPING;
    }

    // Admin approves cancellation request — only if CANCEL_REQUSTED
    /**
     * Can admin approve cancel.
     *
     * @return the boolean result
     */
    public boolean canAdminApproveCancel() {
        return this.orderStatus == OrderStatus.CANCEL_REQUESTED;
    }

    // Admin refuses cancellation request — return to CONFIRMED
    /**
     * Can admin reject cancel.
     *
     * @return the boolean result
     */
    public boolean canAdminRejectCancel() {
        return this.orderStatus == OrderStatus.CANCEL_REQUESTED;
    }

    // Admin confirmed that the goods have arrived in the warehouse
    /**
     * Can admin confirm return.
     *
     * @return the boolean result
     */
    public boolean canAdminConfirmReturn() {
        return this.orderStatus == OrderStatus.RETURN_REQUESTED;
    }

    // State machine transitions to normal state
    /**
     * Can transition to.
     *
     * @param next the next
     * @return the boolean result
     */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this.orderStatus) {
            case PENDING           -> next == OrderStatus.CONFIRMED
                    || next == OrderStatus.CANCELLED;
            case CONFIRMED         -> next == OrderStatus.SHIPPING
                    || next == OrderStatus.CANCEL_REQUESTED;
            case SHIPPING          -> next == OrderStatus.DELIVERED
                    || next == OrderStatus.RETURN_REQUESTED;
            case CANCEL_REQUESTED  -> next == OrderStatus.CANCELLED    // Admin approved
                    || next == OrderStatus.CONFIRMED;   // Admin refused
            case RETURN_REQUESTED  -> next == OrderStatus.RETURNED;
            default                -> false;
        };
    }

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
