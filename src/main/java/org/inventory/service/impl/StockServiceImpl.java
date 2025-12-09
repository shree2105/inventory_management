package org.inventory.service.impl;

import org.inventory.model.Stock;
import org.inventory.repository.InventoryRepository;
import org.inventory.service.EmailService;
import org.inventory.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    private final InventoryRepository inventoryRepository;
    // EmailService may be null in tests (backwards-compatible)
    private EmailService emailService;

    // recipient from application.properties (may be empty in tests)
    @Value("${notification.recipient:}")
    private String notificationRecipient;

    /**
     * Primary constructor used by Spring (real runtime).
     * Spring will inject InventoryRepository and EmailService.
     */
    /**
     * Primary constructor used by Spring (real runtime).
     * Spring will inject InventoryRepository and EmailService.
     */
    @Autowired
    public StockServiceImpl(InventoryRepository inventoryRepository, EmailService emailService) {
        this.inventoryRepository = inventoryRepository;
        this.emailService = emailService;
    }


    /**
     * Backwards-compatible constructor used by unit tests that only pass the repository.
     * Sets emailService to null; send logic checks for null and will skip sending in tests.
     */
    public StockServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
        this.emailService = null; // safe: send logic checks for null
    }

    // ---------- helpers ----------
    private long parseUnitSafe(Object unitObj) {
        if (unitObj == null) return Long.MAX_VALUE;
        if (unitObj instanceof Number) return ((Number) unitObj).longValue();
        try {
            return Long.parseLong(unitObj.toString().trim());
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private void trySendLowStockEmail(Stock s) {
        try {
            long unit = parseUnitSafe(s.getUnit());
            if (unit <= 2L) {
                if (emailService == null) {
                    // In tests or when EmailService not configured, skip sending but log
                    System.out.println("Low stock detected but EmailService not configured. sku=" + s.getProductId());
                    return;
                }
                if (notificationRecipient == null || notificationRecipient.isBlank()) {
                    System.out.println("Low stock detected but notification.recipient not configured. sku=" + s.getProductId());
                    return;
                }
                String subject = "⚠️ Low Stock Alert: " + s.getProductName();
                String body = "Product: " + s.getProductName() + " (ID: " + s.getProductId() + ")\n"
                        + "Model: " + (s.getModel() == null ? "" : s.getModel()) + "\n"
                        + "Units remaining: " + s.getUnit() + "\n\nPlease restock soon.";
                emailService.sendEmailWithAttachment(notificationRecipient, subject, body, null, null);
            }
        } catch (Exception ex) {
            // log but do not throw
            System.err.println("Failed to send low-stock email for product " + s.getProductId() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ---------- StockService methods (kept exactly like your original interface) ----------

    @Override
    public List<Stock> getAllItems(String role, String department) throws Exception {
        try {
            return inventoryRepository.findAll();
        } catch (Exception e) {
            throw new Exception("Failed to fetch all items", e);
        }
    }

    @Override
    public List<Stock> getAllItems() {
        try {
            return inventoryRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all items", e);
        }
    }

    @Override
    public Optional<Stock> getById(Integer id) throws Exception {
        return inventoryRepository.findById(id);
    }

    @Override
    public Stock insertItem(Stock stock) throws Exception {
        // preserve your original totalPrice logic
        stock.setTotalPrice(stock.getPricePerQuantity() * stock.getUnit());
        Stock created = inventoryRepository.insertStock(stock);

        // immediate email if unit <= 2 (non-fatal)
        trySendLowStockEmail(created);

        return created;
    }

    @Override
    public Stock updateItem(Integer id, Stock stock) throws Exception {
        // fetch existing to compare units
        Optional<Stock> existingOpt = inventoryRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new SQLException("Stock not found for id " + id);
        }
        Stock existing = existingOpt.get();
        long oldUnit = parseUnitSafe(existing.getUnit());

        // ensure update object has id and total price
        stock.setProductId(id);
        stock.setTotalPrice(stock.getPricePerQuantity() * stock.getUnit());

        boolean ok = inventoryRepository.updateStock(stock);
        if (!ok) {
            throw new SQLException("No stock updated for id " + id);
        }

        Stock updated = inventoryRepository.findById(id)
                .orElseThrow(() -> new SQLException("Stock not found after update"));

        long newUnit = parseUnitSafe(updated.getUnit());

        // notify only when crossing from >2 to <=2
        if (newUnit <= 2L && (oldUnit > 2L || newUnit < oldUnit)) {
            trySendLowStockEmail(updated);
        }

        return updated;
    }

    @Override
    public boolean deleteItem(Integer id) throws Exception {
        return inventoryRepository.deleteById(id);
    }
}
