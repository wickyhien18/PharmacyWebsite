package com.example.Pharmacy.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/medicines/10")
    public String medicineSingle() {
        return "forward:/e-commerce/shop-single.html";
    }
}
