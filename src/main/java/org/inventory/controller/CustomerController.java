package org.inventory.controller;

import org.inventory.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) { this.customerService = customerService; }

    /**
     * PUT /api/customers/order
     * Expected JSON body (any of):
     * - { "productId": 12, "quantity": 1 }
     * - { "productName": "Widget", "model": "W100", "quantity": 1 }
     *
     * Returns 200 on success, 409 on business failure (insufficient), 400 on bad request.
     */
    @PutMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> resp = customerService.placeOrder(body);
            String status = resp.getOrDefault("status", "FAILED").toString();
            if ("PLACED".equalsIgnoreCase(status)) {
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(409).body(resp);
            }
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", iae.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", "FAILED", "message", "Server error: " + ex.getMessage()));
        }
    }
}
