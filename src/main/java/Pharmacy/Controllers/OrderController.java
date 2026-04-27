package Pharmacy.Controllers;

import Pharmacy.Entities.Orders;
import Pharmacy.Services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order API", description = "Quản lý danh sách đơn thuốc")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    @Operation(description = "Lấy danh sách đơn thuốc")
    public List<Orders> getAll() {
        return orderService.getAll();
    }
}
