package Pharmacy.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/medicines/1")
    public String medicineSingle() {
        return "forward:/e-commerce/shop-single.html";
    }
}
