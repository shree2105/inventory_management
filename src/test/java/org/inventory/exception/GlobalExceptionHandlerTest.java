package org.inventory.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the actual handler methods present in GlobalExceptionHandler.java
 * (matches the methods that exist in your project: handleResourceNotFound and handleGeneralException).
 */
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returnsNotFound_and_message() {
        ResourceNotFoundException ex = new ResourceNotFoundException("no-such-record");
        ResponseEntity<String> resp = handler.handleResourceNotFound(ex);

        assertNotNull(resp, "ResponseEntity should not be null");
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode(), "ResourceNotFoundException should map to 404");
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("no-such-record"),
                "Handler should include original exception message in body");
    }

    @Test
    void handleGeneralException_returnsInternalServerError_and_message() {
        Exception ex = new Exception("some-general-error");
        ResponseEntity<String> resp = handler.handleGeneralException(ex);

        assertNotNull(resp);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode(), "General exceptions should map to 500");
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().toLowerCase().contains("error"), "Response body should mention 'Error'");
        assertTrue(resp.getBody().contains("some-general-error"), "Original message should be included");
    }
}
