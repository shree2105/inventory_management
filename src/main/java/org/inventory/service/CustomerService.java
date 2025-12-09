package org.inventory.service;

import java.util.Map;

public interface CustomerService {
    /**
     * placeOrder - accepts raw request data in a Map (no DTO).
     * Expected keys:
     *  - "productId" (Integer) OR ("productName" (String) + optional "model" (String))
     *  - "quantity" (Integer)
     *  - optionally "customerName", "customerEmail"
     *
     * Returns a Map with at least:
     *  - "status" => "PLACED" or "FAILED"
     *  - "message" => explanation
     *  - optionally "productId", "newUnits"
     */
    Map<String, Object> placeOrder(Map<String, Object> request) throws Exception;
}
