package org.inventory.service.impl;

import org.inventory.model.Stock;
import org.inventory.repository.InventoryRepository;
import org.inventory.service.CustomerService;
import org.inventory.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final InventoryRepository repo;
    private final EmailService emailService;

    @Value("${notification.recipient:}")
    private String notificationRecipient;

    public CustomerServiceImpl(InventoryRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    private long toLongSafe(Object o) {
        if (o == null) return Long.MAX_VALUE;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString().trim());
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> placeOrder(Map<String, Object> request) throws Exception {
        // ✅ 1. Validate quantity
        Object qtyObj = request.get("quantity");
        if (qtyObj == null) throw new IllegalArgumentException("quantity is required");
        int quantity;
        try {
            quantity = Integer.parseInt(qtyObj.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("quantity must be a number");
        }
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");

        // ✅ 2. Resolve product (id preferred)
        Stock stock = null;
        Object pidObj = request.get("productId");
        if (pidObj != null) {
            int pid;
            try {
                pid = Integer.parseInt(pidObj.toString());
            } catch (Exception e) {
                throw new IllegalArgumentException("productId must be an integer");
            }
            Optional<Stock> opt = repo.findById(pid);
            if (opt.isPresent()) stock = opt.get();
        } else {
            Object nameObj = request.get("productName");
            if (nameObj == null) throw new IllegalArgumentException("productId or productName required");
            String pname = nameObj.toString();
            Object modelObj = request.get("model");
            String pmodel = modelObj == null ? null : modelObj.toString();

            Optional<Stock> opt = repo.findAll().stream()
                    .filter(s -> s.getProductName() != null
                            && s.getProductName().equalsIgnoreCase(pname)
                            && (pmodel == null || pmodel.equalsIgnoreCase(s.getModel())))
                    .findFirst();
            if (opt.isPresent()) stock = opt.get();
        }

        if (stock == null) {
            return Map.of("status", "FAILED", "message", "Product not found");
        }

        // ✅ 3. Check & update stock
        long available = toLongSafe(stock.getUnit());
        if (available < quantity) {
            return Map.of("status", "FAILED", "message", "Insufficient stock. Available: " + available);
        }

        long after = available - quantity;
        int remainingUnits = (int) after;

        stock.setUnit(remainingUnits);
        boolean ok = repo.updateStock(stock);
        if (!ok) throw new SQLException("Failed to update stock id " + stock.getProductId());

        System.out.println(
                "DEBUG STOCK: productId=" + stock.getProductId()
                        + ", available(before)=" + available
                        + ", quantity=" + quantity
                        + ", remaining(after)=" + remainingUnits
        );

        // ✅ 3.5 Customer details from request
        String customerName = request.get("customerName") == null
                ? "Unknown Customer"
                : request.get("customerName").toString();

        Object addrObj = null;
        if (request.containsKey("customerAddress")) {
            addrObj = request.get("customerAddress");
        } else if (request.containsKey("address")) {
            addrObj = request.get("address");
        } else if (request.containsKey("Address")) {
            addrObj = request.get("Address");
        }

        String customerAddress = (addrObj == null)
                ? "No address provided"
                : addrObj.toString();

        System.out.println("DEBUG CUSTOMER: name=" + customerName + ", address=" + customerAddress);

        // ✅ 4. Order placed email (ALWAYS on success)
        try {
            System.out.println("Order mail: emailService=" + (emailService != null)
                    + ", recipient=" + notificationRecipient);

            if (emailService != null && notificationRecipient != null && !notificationRecipient.isBlank()) {
                String subject = "✅ Order Placed: " + stock.getProductName();
                String body = buildOrderEmailHtml(stock, quantity, remainingUnits, customerName, customerAddress);
                // body is HTML now
                emailService.sendEmailWithAttachment(notificationRecipient, subject, body, null, null);
                System.out.println("Order placed email sent for product " + stock.getProductId());
            } else {
                System.out.println("Skipping order-placed email (emailService/config missing) for product " + stock.getProductId());
            }
        } catch (Exception ex) {
            System.err.println("Failed to send order-placed email: " + ex.getMessage());
            ex.printStackTrace();
            // don't rollback
        }

        // ✅ 5. Low-stock / restock email (ONLY when remainingUnits <= 2)
        if (remainingUnits <= 2) {
            try {
                System.out.println("Low stock condition met (remainingUnits=" + remainingUnits + "), attempting low-stock mail.");
                if (emailService != null && notificationRecipient != null && !notificationRecipient.isBlank()) {
                    String subject = "⚠️ Low Stock Alert: " + stock.getProductName();
                    String body = buildLowStockEmailHtml(stock, remainingUnits);
                    emailService.sendEmailWithAttachment(notificationRecipient, subject, body, null, null);
                    System.out.println("Low-stock email SENT for product " + stock.getProductId());
                } else {
                    System.out.println("Skipping low-stock email (emailService/config missing) for product " + stock.getProductId());
                }
            } catch (Exception ex) {
                System.err.println("Failed to send low-stock email: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            System.out.println("Low-stock email NOT sent, remainingUnits=" + remainingUnits + " (>2).");
        }

        // ✅ 6. Response back to frontend
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "PLACED");
        resp.put("message", "Order placed successfully");
        resp.put("productId", stock.getProductId());
        resp.put("newUnits", remainingUnits);
        return resp;
    }

    // ------------------ HTML builders ------------------

    private String buildOrderEmailHtml(Stock stock,
                                       int quantity,
                                       int remaining,
                                       String customerName,
                                       String customerAddress) {

        String productName = stock.getProductName() == null ? "" : stock.getProductName();
        String model = stock.getModel() == null ? "" : stock.getModel();

        return ""
                + "<!doctype html>"
                + "<html><head><meta charset='utf-8'>"
                + "<title>Order Placed</title></head>"
                + "<body style='margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,sans-serif;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' "
                + "style='background-color:#f4f4f4;padding:20px 0;'>"
                + "<tr><td align='center'>"

                + "<table role='presentation' width='600' cellspacing='0' cellpadding='0' "
                + "style='background-color:#ffffff;border-radius:8px;overflow:hidden;"
                + "box-shadow:0 2px 8px rgba(0,0,0,0.06);'>"

                + "<tr><td align='center' "
                + "style='background:linear-gradient(90deg,#1e88e5,#42a5f5);"
                + "padding:16px 24px;'>"
                + "<span style='color:#ffffff;font-size:20px;font-weight:bold;letter-spacing:0.5px;'>"
                + "Inventory Notification"
                + "</span></td></tr>"

                + "<tr><td style='padding:24px 28px 8px 28px;'>"
                + "<h1 style='margin:0;font-size:20px;color:#333333;'>New Order Placed</h1>"
                + "<p style='margin:8px 0 0 0;font-size:14px;color:#666666;line-height:1.6;'>"
                + "A new order has been placed in your inventory system. Here are the details:"
                + "</p></td></tr>"

                + "<tr><td style='padding:12px 28px 0 28px;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0'>"
                + "<tr><td style='background-color:#f5f9ff;border:1px solid #d0e3ff;"
                + "border-radius:6px;padding:12px 16px;'>"
                + "<p style='margin:0;font-size:14px;color:#333333;'>"
                + "<strong>Product:</strong> " + productName + "<br/>"
                + "<strong>Model:</strong> " + model + "<br/>"
                + "<strong>Quantity ordered:</strong> " + quantity + "<br/>"
                + "<strong>Remaining units:</strong> " + remaining + "<br/>"
                + "<strong>Customer:</strong> " + customerName + "<br/>"
                + "<strong>Address:</strong> " + customerAddress + "<br/>"
                + "<strong>Product ID:</strong> " + stock.getProductId()
                + "</p></td></tr></table></td></tr>"

                + "<tr><td align='center' style='padding:20px 28px 4px 28px;'>"
                + "<a href='#' style='display:inline-block;padding:10px 18px;font-size:14px;"
                + "font-weight:600;text-decoration:none;background-color:#1e88e5;color:#ffffff;"
                + "border-radius:4px;'>Open Dashboard</a>"
                + "</td></tr>"

                + "<tr><td style='padding:4px 28px 20px 28px;'>"
                + "<p style='margin:0;font-size:12px;color:#999999;line-height:1.5;'>"
                + "If this order was not expected, please review recent activity in your system."
                + "</p></td></tr>"

                + "<tr><td align='center' style='padding:14px 24px 18px 24px;"
                + "background-color:#fafafa;border-top:1px solid #eeeeee;'>"
                + "<p style='margin:0;font-size:11px;color:#aaaaaa;'>"
                + "&copy; " + java.time.Year.now() + " Inventory System. "
                + "This is an automated message, please do not reply."
                + "</p></td></tr>"

                + "</table></td></tr></table></body></html>";
    }

    private String buildLowStockEmailHtml(Stock stock, int remaining) {
        String productName = stock.getProductName() == null ? "" : stock.getProductName();
        String model = stock.getModel() == null ? "" : stock.getModel();

        return ""
                + "<!doctype html>"
                + "<html><head><meta charset='utf-8'>"
                + "<title>Low Stock Alert</title></head>"
                + "<body style='margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,sans-serif;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' "
                + "style='background-color:#f4f4f4;padding:20px 0;'>"
                + "<tr><td align='center'>"

                + "<table role='presentation' width='600' cellspacing='0' cellpadding='0' "
                + "style='background-color:#ffffff;border-radius:8px;overflow:hidden;"
                + "box-shadow:0 2px 8px rgba(0,0,0,0.06);'>"

                + "<tr><td align='center' "
                + "style='background:linear-gradient(90deg,#e65100,#ff9800);"
                + "padding:16px 24px;'>"
                + "<span style='color:#ffffff;font-size:20px;font-weight:bold;letter-spacing:0.5px;'>"
                + "Low Stock Alert"
                + "</span></td></tr>"

                + "<tr><td style='padding:24px 28px 8px 28px;'>"
                + "<h1 style='margin:0;font-size:20px;color:#333333;'>Product Reaching Minimum Level</h1>"
                + "<p style='margin:8px 0 0 0;font-size:14px;color:#666666;line-height:1.6;'>"
                + "One of your products has reached the low stock threshold. Please review and restock soon."
                + "</p></td></tr>"

                + "<tr><td style='padding:12px 28px 0 28px;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0'>"
                + "<tr><td style='background-color:#fff8e1;border:1px solid #ffe0b2;"
                + "border-radius:6px;padding:12px 16px;'>"
                + "<p style='margin:0;font-size:14px;color:#333333;'>"
                + "<strong>Product:</strong> " + productName + "<br/>"
                + "<strong>Model:</strong> " + model + "<br/>"
                + "<strong>Current stock:</strong> " + remaining + "<br/>"
                + "<strong>Product ID:</strong> " + stock.getProductId() + "<br/>"
                + "Please restock soon to avoid stock-out."
                + "</p></td></tr></table></td></tr>"

                + "<tr><td align='center' style='padding:20px 28px 4px 28px;'>"
                + "<a href='#' style='display:inline-block;padding:10px 18px;font-size:14px;"
                + "font-weight:600;text-decoration:none;background-color:#e65100;color:#ffffff;"
                + "border-radius:4px;'>Open Inventory</a>"
                + "</td></tr>"

                + "<tr><td align='center' style='padding:14px 24px 18px 24px;"
                + "background-color:#fafafa;border-top:1px solid #eeeeee;'>"
                + "<p style='margin:0;font-size:11px;color:#aaaaaa;'>"
                + "&copy; " + java.time.Year.now() + " Inventory System. "
                + "This is an automated message, please do not reply."
                + "</p></td></tr>"

                + "</table></td></tr></table></body></html>";
    }
}
