package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Carts;
import com.example.Pharmacy.Services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Cart API", description = "Quản lý danh sách thuốc trong giỏ")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @Operation(description = "Lấy danh sách thuốc trong giỏ")
    public List<Carts> getAll() {
        return cartService.getAll();
    }

}
