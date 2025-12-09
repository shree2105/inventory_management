package org.inventory.service;

import org.inventory.model.Staff;
import org.inventory.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
public class AuthService {

    private final StaffRepository staffRepository;

    public AuthService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    // LOGIN with email + phone + password
    public LoginResponse login(String email, String phone, String password) throws SQLException {

        // Admin hardcoded logic
        if ("admin@example.com".equalsIgnoreCase(email)
                && "9999999999".equals(phone)
                && "admin123".equals(password)) {

            // Check if admin-secret needed
            return LoginResponse.secretRequired();
        }

        // Normal user
        Optional<Staff> user = staffRepository.findByEmailAndPhoneNumber(email, phone);
        if (user.isEmpty()) {
            return LoginResponse.fail("Invalid email or phone");
        }

        Staff s = user.get();

        // DB stores plain password
        if (!s.getPassword().equals(password)) {
            return LoginResponse.fail("Invalid password");
        }

        return LoginResponse.success("Login successful", s);
    }

    // SIGNUP
    public SignupResponse signup(Staff staff) throws SQLException {

        if (staffRepository.existsByEmail(staff.getEmail())) {
            return new SignupResponse(false, "Email already exists");
        }

        if (staffRepository.existsByPhoneNumber(staff.getPhoneNumber())) {
            return new SignupResponse(false, "Phone number already exists");
        }

        Staff created = staffRepository.insert(staff);
        return new SignupResponse(true, "Signup successful");
    }

    // ----------- RESPONSE TYPES -----------

    public record LoginResponse(boolean success, boolean requireSecret, String message, Staff staff) {

        public static LoginResponse success(String msg, Staff s) {
            return new LoginResponse(true, false, msg, s);
        }

        public static LoginResponse fail(String msg) {
            return new LoginResponse(false, false, msg, null);
        }

        // Renamed to avoid conflict
        public static LoginResponse secretRequired() {
            return new LoginResponse(false, true, "Admin secret required", null);
        }
    }

    public record SignupResponse(boolean success, String message) { }
}
