package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payments,Long> {
}
