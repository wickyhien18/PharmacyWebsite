package Pharmacy.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Specifies that this class is a JPA entity mapped to a database table.
@Entity
// Specifies the database table used for mapping this entity.
@Table(name = "shipments")
@Getter @Setter
// Generates a no-argument constructor via Lombok.
@NoArgsConstructor
// Generates an all-arguments constructor via Lombok.
@AllArgsConstructor
// Produces complex builder APIs for this class via Lombok.
@Builder
/**
 * Database Entity for Shipment.
 * This class is used to map data and handle basic structure.
 */
public class Shipment {

    public enum ShipmentStatus { PENDING, SHIPPING, DELIVERED, FAILED }

    // Specifies the primary key of this entity.
    @Id
    // Configures the way of incrementing the specified column(field).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Specifies the mapped column for a persistent property or field.
    @Column(name = "shipment_id")
    private Long shipmentId;

    // Specifies a single-valued association to another entity class that has many-to-one multiplicity.
    @ManyToOne(fetch = FetchType.LAZY)
    // Specifies a column for joining an entity association or element collection.
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    // Specifies the mapped column for a persistent property or field.
    @Column(length = 100)
    private String carrier;    // GHN, GHTK, VNPost...

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    // Specifies the mapped column for a persistent property or field.
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}