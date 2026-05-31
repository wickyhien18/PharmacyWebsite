package Pharmacy.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    //Main page
    @GetMapping("/")
    public String home() {
        return "forward:/e-commerce/index.html";
    }
}
