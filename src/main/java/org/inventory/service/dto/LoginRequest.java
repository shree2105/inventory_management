package org.inventory.service.dto;

public class LoginRequest {
    public String email;
    public String phoneNumber;
    public String password;     // optional for admin flow if you use password auth
    public String adminSecret;  // optional

    // getters/setters (or use Lombok if available)
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAdminSecret() { return adminSecret; }
    public void setAdminSecret(String adminSecret) { this.adminSecret = adminSecret; }
}
