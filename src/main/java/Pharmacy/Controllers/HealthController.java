package Pharmacy.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Indicates that this class is a REST controller handling HTTP requests.
@RestController
// Maps HTTP requests to the controller or handler method.
@RequestMapping("/api")
/**
 * Class HealthController.
 * Provides functionality and data modeling for HealthController.
 */
public class HealthController {

    // Maps HTTP GET requests to this handler method.
    @GetMapping("/health")
    /**
     * Health.
     *
     * @return the ResponseEntity<String> result
     */
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
