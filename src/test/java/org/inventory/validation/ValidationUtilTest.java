package org.inventory.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.inventory.model.Stock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Stock stock = new Stock();
        stock.setProductName("Laptop");
        stock.setModel("Dell XPS");
        stock.setPricePerQuantity(1200.0);
        stock.setUnit(10);
        stock.setTotalPrice(12000.0);
        stock.setStatus("ACTIVE");

        Set<ConstraintViolation<Stock>> violations = validator.validate(stock);
        assertTrue(violations.isEmpty(), "Expected no validation errors");
    }

    @Test
    void whenFieldsInvalid_thenViolationsOccur() {
        Stock stock = new Stock(); // missing all required fields
        stock.setProductName(""); // invalid @NotBlank
        stock.setModel("");       // invalid @NotBlank

        Set<ConstraintViolation<Stock>> violations = validator.validate(stock);

        assertFalse(violations.isEmpty(), "Expected validation errors");

        boolean hasProductNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("productName"));
        boolean hasModelError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("model"));

        assertTrue(hasProductNameError, "Expected productName validation error");
        assertTrue(hasModelError, "Expected model validation error");
    }
}
