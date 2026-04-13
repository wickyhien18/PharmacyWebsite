package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Payments;
import com.example.Pharmacy.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payments> getAll() {
        return paymentRepository.getAll();
    }

    public Payments insert(Payments Payments) {
        return paymentRepository.save(Payments);
    }

    public Payments update(Integer id, Payments Payments) {
        Payments Payments1 = paymentRepository.findByIdDetail(id);
        Payments1.setStatus(Payments.getStatus());
        Payments1.setPayment_method(Payments.getPayment_method());
        Payments1.setAmount(Payments.getAmount());
        Payments1.setOrders(Payments.getOrders());
        return paymentRepository.save(Payments1);
    }


    public void delete(Integer id) {
        paymentRepository.deleteById(id.longValue());
    }
}
