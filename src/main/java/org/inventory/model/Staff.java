
package org.inventory.model;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
public class Staff {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer staffId;



    @Size(min = 4, max = 50, message = "Password must be 4–50 characters")


    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String staffName;

    // Optional now
    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String designation;

    // Optional but with allowed values
    @Pattern(regexp = "^(Admin|Staff)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Rights must be Admin or Staff")
    private String rights;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 254)
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String phoneNumber;

    // Optional but controlled
    @Pattern(regexp = "^(Active|Inactive)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Status must be Active or Inactive")
    private String status;
@Column(updatable = false)
private LocalDateTime createdDate;

@Column
private LocalDateTime updatedDate;


    public Staff() {}

    // Getters and setters
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) {
        this.staffName = staffName == null ? null : staffName.trim();
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) {
        this.department = department == null ? null : department.trim();
    }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) {
        this.designation = designation == null ? null : designation.trim();
    }

    public String getRights() { return rights; }
    public void setRights(String rights) {
        this.rights = rights == null ? null : rights.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null ? null : phoneNumber.trim();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
