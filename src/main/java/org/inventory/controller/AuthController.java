package org.inventory.controller;

import org.inventory.model.Staff;
import org.inventory.repository.StaffRepository;
import org.inventory.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ADMIN CONFIG (hardcoded via properties)
    @Value("${app.admin.email:admin@gmail.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.secret:super-secret-key}")
    private String adminSecret;

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_RX = Pattern.compile("^\\d{10}$");

    public AuthController(StaffRepository staffRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ---------------------------------------
    // LOGIN (email + password [+ adminSecret])
    // ---------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String password = req.get("password");
        String adminSecretInput = req.get("adminSecret");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password required"));
        }

        if (!EMAIL_RX.matcher(email).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid email format"));
        }

        // ===== ADMIN LOGIN =====
        if (adminEmail.equalsIgnoreCase(email) && adminPassword.equals(password)) {

            if (adminSecretInput == null || adminSecretInput.isEmpty()) {
                return ResponseEntity.status(428)
                        .body(Map.of(
                                "action", "require_secret",
                                "message", "Admin secret required"
                        ));
            }

            if (!adminSecret.equals(adminSecretInput)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid admin secret"));
            }

            Map<String, Object> claims = Map.of(
                    "rights", "Admin",
                    "staffId", 0
            );

            String token = jwtService.generateToken(adminEmail, claims);

            return ResponseEntity.ok(Map.of(
                    "message", "Admin login successful",
                    "status", "SUCCESS",
                    "token", token,
                    "data", Map.of(
                            "email", adminEmail,
                            "rights", "Admin"
                    )
            ));
        }

        // ===== STAFF LOGIN =====
        try {
            Optional<Staff> user = staffRepository.findByEmail(email);

            if (user.isEmpty()) {
                System.out.println("[DEBUG LOGIN] No staff found for email = " + email);
                // For now, send a more explicit message so you can see which case is failing
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "No account found for this email"));
            }

            Staff s = user.get();
            String stored = s.getPassword();
            boolean match = false;

            System.out.println("[DEBUG LOGIN] Found staff for email = " + email);
            System.out.println("[DEBUG LOGIN] Stored password in DB = " + stored);

            if (stored != null) {
                if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                    System.out.println("[DEBUG LOGIN] Treating stored password as BCrypt");
                    match = passwordEncoder.matches(password, stored);
                } else {
                    System.out.println("[DEBUG LOGIN] Treating stored password as PLAINTEXT");
                    match = password.equals(stored);
                }
            } else {
                System.out.println("[DEBUG LOGIN] Stored password is NULL");
            }

            System.out.println("[DEBUG LOGIN] Password match result = " + match);

            if (!match) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Incorrect password"));
            }

            // Generate JWT
            Map<String, Object> claims = Map.of(
                    "rights", s.getRights(),
                    "staffId", s.getStaffId()
            );

            String token = jwtService.generateToken(s.getEmail(), claims);

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "status", "SUCCESS",
                    "token", token,
                    "data", Map.of(
                            "staffId", s.getStaffId(),
                            "email", s.getEmail(),
                            "rights", s.getRights(),
                            "name", s.getStaffName()
                    )
            ));
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error", "detail", e.getMessage()));
        }
    }

    // ---------------------------------------
    // SIGNUP (Staff)
    // ---------------------------------------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> req) {

        String name = req.get("name");
        String email = req.get("email");
        String phone = req.get("phoneNumber");
        String password = req.get("password");

        if (name == null || name.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Name required"));

        if (email == null || !EMAIL_RX.matcher(email).matches())
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid email"));

        if (phone == null || !PHONE_RX.matcher(phone).matches())
            return ResponseEntity.badRequest().body(Map.of("message", "Phone must be 10 digits"));

        if (password == null || password.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 8 characters"));

        try {
            if (staffRepository.existsByEmail(email))
                return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));

            if (staffRepository.existsByPhoneNumber(phone))
                return ResponseEntity.badRequest().body(Map.of("message", "Phone already exists"));

            String encodedPassword = passwordEncoder.encode(password);

            Staff s = new Staff();
            s.setStaffName(name);
            s.setEmail(email);
            s.setPhoneNumber(phone);
            s.setPassword(encodedPassword);
            s.setRights("Staff");
            s.setStatus("ACTIVE");

            staffRepository.insert(s);

            System.out.println("[DEBUG SIGNUP] Created staff " + email + " with encoded password = " + encodedPassword);

            return ResponseEntity.ok(Map.of(
                    "message", "Account created successfully",
                    "status", "SUCCESS"
            ));
        }
        catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Database error", "detail", e.getMessage()));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error", "detail", ex.getMessage()));
        }
    }
}
