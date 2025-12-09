package org.inventory.controller;
import jakarta.validation.Valid;
import org.inventory.model.Staff;
import org.inventory.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }



    // Create new staff item
 // POST http://localhost:8080/api/staff/addStaff
    @PostMapping("/addStaff")
    public ResponseEntity<?> createStaff(@Valid @RequestBody Staff s) {
        try {
            Staff created = staffService.createStaff(s);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }




    // Get all staff
    // GET http://localhost:8080/api/staff/getAllStaff
    @GetMapping("/getAllStaff")
    public ResponseEntity<?> getAllStaff() {
        try {
            List<Staff> list = staffService.getAllStaff();
            return ResponseEntity.ok(list);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB error");
        }
    }

    // Get staff by id
    // GET http://localhost:8080/api/staff/getStaffById/{id}
    @GetMapping("/getStaffById/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable("id") int id) {
        try {
            Optional<Staff> opt = staffService.getStaffById(id);
            return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found"));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB error");
        }
    }

    // Update staff
    //  http://localhost:8080/api/staff/updateStaff/{id}
    @PutMapping("/updateStaff/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable("id") int id, @Valid @RequestBody Staff s) {
        try {
            s.setStaffId(id);
            boolean ok = staffService.updateStaff(s);
            if (ok) {
                Optional<Staff> updated = staffService.getStaffById(id);
                return updated.<ResponseEntity<?>>map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.ok(s)); // return provided object if service doesn't re-fetch
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB error");
        }
    }

    // Delete staff
    //  http://localhost:8080/api/staff/deleteStaff/{id}
    @DeleteMapping("/deleteStaff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable("id") int id) {
        try {
            boolean ok = staffService.deleteStaff(id);
            if (ok) return ResponseEntity.noContent().build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB error");
        }
    }
}
