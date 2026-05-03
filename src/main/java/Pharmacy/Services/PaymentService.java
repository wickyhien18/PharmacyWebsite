package Pharmacy.Services;

import Pharmacy.Entities.Payments;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payments> getAll() {
        return paymentRepository.findAll();
    }

    public Payments insert(Payments Payments) {
        return paymentRepository.save(Payments);
    }

    public Payments update(Long id, Payments Payments) {
        Payments Payments1 = paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment",id));
        Payments1.setOrders(Payments.getOrders());
        Payments1.setPaymentMethod(Payments.getPaymentMethod());
        Payments1.setAmount(Payments.getAmount());
        Payments1.setTransactionCode(Payments.getTransactionCode());
        Payments1.setStatus(Payments.getStatus());
        Payments1.setPaidAt(Payments.getPaidAt());
        return paymentRepository.save(Payments1);
    }


    public void delete(Integer id) {
        paymentRepository.deleteById(id.longValue());
    }
}
