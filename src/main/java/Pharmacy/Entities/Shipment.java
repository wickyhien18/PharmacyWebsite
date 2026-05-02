package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    public enum ShipmentStatus { PENDING, SHIPPING, DELIVERED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long shipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(length = 100)
    private String carrier;    // GHN, GHTK, VNPost...

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}