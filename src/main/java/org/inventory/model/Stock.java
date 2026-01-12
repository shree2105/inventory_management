package org.inventory.model;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventorystock")

public class Stock {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer productId;


    @NotBlank(message = "productName is required")
    @Size(max = 100, message = "productName must be at most 100 characters")
    private String productName;


    @NotBlank(message = "model is required")
    @Size(max = 100, message = "model must be at most 100 characters")
    private String model;

    @NotNull(message = "pricePerQuantity is required")
    @Positive(message = "pricePerQuantity must be positive")
    private Double pricePerQuantity;

    @NotNull(message = "unit is required")
    @Min(value = 0, message = "unit must be zero or positive")
    private Integer unit;

    // computed server-side: pricePerQuantity * unit
    private Double totalPrice;

    @NotBlank(message = "status is required")
    @Pattern(regexp = "^(active|inactive)$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "status must be 'active' or 'inactive'")
    private String status;
@Column(updatable = false)
private LocalDateTime createdDate;

@Column
private LocalDateTime updatedDate;


    public Stock() {}

    // Getters and setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getPricePerQuantity() { return pricePerQuantity; }
    public void setPricePerQuantity(Double pricePerQuantity) { this.pricePerQuantity = pricePerQuantity; }

    public Integer getUnit() { return unit; }
    public void setUnit(Integer unit) { this.unit = unit; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate;}
}
