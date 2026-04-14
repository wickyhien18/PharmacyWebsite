package com.example.Pharmacy.Controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Cart API", description = "Quản lý danh sách thuốc trong rỏ")
public class CartController {

}
