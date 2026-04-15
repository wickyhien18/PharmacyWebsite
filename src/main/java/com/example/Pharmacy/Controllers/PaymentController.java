package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Payments;
import com.example.Pharmacy.Services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment API", description = "Quản lý thanh toán")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    @Operation(description = "Lấy danh sách thanh toán")
    public List<Payments> getAll() {
        return paymentService.getAll();
    }
}
