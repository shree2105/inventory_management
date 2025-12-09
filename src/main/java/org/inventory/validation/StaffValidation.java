//package org.inventory.validation;
//
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.Validation;
//import jakarta.validation.Validator;
//import jakarta.validation.ValidatorFactory;
//import org.inventory.model.Staff;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Validation tests for Staff model.
// * - Assumes Staff has jakarta.validation annotations (as provided earlier).
// */
//public class StaffValidationTest {
//
//    private static ValidatorFactory factory;
//    private static Validator validator;
//
//    @BeforeAll
//    static void setup() {
//        factory = Validation.buildDefaultValidatorFactory();
//        validator = factory.getValidator();
//    }
//
//    @AfterAll
//    static void teardown() {
//        factory.close();
//    }
//
//    @Test
//    void validStaff_hasNoViolations() {
//        Staff s = new Staff();
//        s.setStaffName("Raja Shree");
//        s.setEmail("raja@example.com");
//        s.setPhoneNumber("9876543210");
//        s.setDepartment("IT");
//        s.setDesignation("Engineer");
//        s.setRights("READ,WRITE");
//        s.setStatus("ACTIVE");
//
//        Set<ConstraintViolation<Staff>> violations = validator.validate(s);
//        assertTrue(violations.isEmpty(), "Expected no constraint violations for a valid staff");
//    }
//
//    @Test
//    void staffName_withDigits_violates() {
//        Staff s = new Staff();
//        s.setStaffName("Raja123");
//        s.setEmail("r@x.com");
//        s.setPhoneNumber("9876543210");
//        s.setStatus("ACTIVE");
//
//        Set<ConstraintViolation<Staff>> v = validator.validate(s);
//        assertFalse(v.isEmpty());
//        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("staffName")),
//                "Expected staffName validation error");
//    }
//
//    @Test
//    void blankName_violates() {
//        Staff s = new Staff();
//        s.setStaffName("   ");
//        s.setEmail("r@x.com");
//        s.setPhoneNumber("9876543210");
//        s.setStatus("ACTIVE");
//
//        Set<ConstraintViolation<Staff>> v = validator.validate(s);
//        assertFalse(v.isEmpty());
//        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("staffName")));
//    }
//
//    @Test
//    void invalidEmail_violates() {
//        Staff s = new Staff();
//        s.setStaffName("Raja");
//        s.setEmail("not-an-email");
//        s.setPhoneNumber("9876543210");
//        s.setStatus("ACTIVE");
//
//        Set<ConstraintViolation<Staff>> v = validator.validate(s);
//        assertFalse(v.isEmpty());
//        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("email")),
//                "Expected email validation error");
//    }
//
//    @Test
//    void phoneNot10Digits_violates() {
//        Staff s = new Staff();
//        s.setStaffName("Raja");
//        s.setEmail("r@x.com");
//        s.setPhoneNumber("12345"); // too short
//        s.setStatus("ACTIVE");
//
//        Set<ConstraintViolation<Staff>> v = validator.validate(s);
//        assertFalse(v.isEmpty());
//        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("phoneNumber")),
//                "Expected phoneNumber validation error");
//    }
//
//    @Test
//    void invalidStatus_violates() {
//        Staff s = new Staff();
//        s.setStaffName("Raja");
//        s.setEmail("r@x.com");
//        s.setPhoneNumber("9876543210");
//        s.setStatus("UNKNOWN"); // not allowed by pattern
//
//        Set<ConstraintViolation<Staff>> v = validator.validate(s);
//        assertFalse(v.isEmpty());
//        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("status")),
//                "Expected status validation error");
//    }
//
//    @Test
//    void multipleViolations_reported() {
//        Staff s = new Staff();
//        s.setStaffName("1");              // invalid (too short & digits)
//        s.setEmail("bad-email");          // invalid
//        s.setPhoneNumber("abc");          // invalid
//        s.setStatus("BAD");               // invalid
//
//        Set<ConstraintViolation<Staff>> v = validator.validate(s);
//        assertTrue(v.size() >= 3, "Expected multiple violations (found: " + v.size() + ")");
//    }
//}
