package Pharmacy.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.orm.jpa.persistenceunit.SpringPersistenceUnitInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public enum OrderStatus  { PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, CANCEL_REQUESTED, RETURN_REQUESTED, RETURNED }
    public enum PaymentStatus { PENDING, PAID, FAILED, REFUNDED }

    //Primary key
    @Id

    //Id auto_increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    //Mapping with column in table in database
    @Column(name = "order_id")
    private Long orderId;

    //N - 1 Relationship
    //FetchType = LAZY: only load when using query, = EAGER: alway load
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "order_code", nullable = false, unique = true, length = 100)
    private String orderCode;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "cancelled_by", length = 20)
    private String cancelledBy;          // USER / ADMIN

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;      // Lý do huỷ / hoàn

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //1 - N Relationship
    //Mapped by another entities
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderItems> orderItems = new ArrayList<>();

    // ================================================================
    // BUSINESS RULES
    // ================================================================

    // User tự huỷ ngay — chỉ khi PENDING
    public boolean canUserCancelDirectly() {
        return this.orderStatus == OrderStatus.PENDING;
    }

    // User gửi yêu cầu huỷ — chỉ khi CONFIRMED
    // Admin sẽ duyệt sau
    public boolean canUserRequestCancel() {
        return this.orderStatus == OrderStatus.CONFIRMED;
    }

    // User gửi yêu cầu hoàn hàng — chỉ khi SHIPPING
    public boolean canUserRequestReturn() {
        return this.orderStatus == OrderStatus.SHIPPING;
    }

    // Admin duyệt yêu cầu huỷ — chỉ khi CANCEL_REQUESTED
    public boolean canAdminApproveCancel() {
        return this.orderStatus == OrderStatus.CANCEL_REQUESTED;
    }

    // Admin từ chối yêu cầu huỷ — quay về CONFIRMED
    public boolean canAdminRejectCancel() {
        return this.orderStatus == OrderStatus.CANCEL_REQUESTED;
    }

    // Admin xác nhận hàng đã về kho
    public boolean canAdminConfirmReturn() {
        return this.orderStatus == OrderStatus.RETURN_REQUESTED;
    }

    // State machine chuyển trạng thái thông thường
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this.orderStatus) {
            case PENDING           -> next == OrderStatus.CONFIRMED
                    || next == OrderStatus.CANCELLED;
            case CONFIRMED         -> next == OrderStatus.SHIPPING
                    || next == OrderStatus.CANCEL_REQUESTED;
            case SHIPPING          -> next == OrderStatus.DELIVERED
                    || next == OrderStatus.RETURN_REQUESTED;
            case CANCEL_REQUESTED  -> next == OrderStatus.CANCELLED    // Admin duyệt
                    || next == OrderStatus.CONFIRMED;   // Admin từ chối
            case RETURN_REQUESTED  -> next == OrderStatus.RETURNED;
            default                -> false;
        };
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
