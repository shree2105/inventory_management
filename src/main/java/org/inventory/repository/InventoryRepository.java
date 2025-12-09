package org.inventory.repository;

import org.inventory.config.dbconfig;
import org.inventory.model.Stock;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InventoryRepository {
    public Stock insertStock(Stock stock) throws SQLException {
        Optional<Stock> existing = findByModel(stock.getModel());
        if (existing.isPresent()) {
            // same model found → update quantity
            Stock old = existing.get();
            int newUnit = old.getUnit() + stock.getUnit();
            double newTotal = newUnit * old.getPricePerQuantity();

            String updateSql = "UPDATE inventorystock SET unit = ?, total_price = ? WHERE model = ?";
            try (Connection conn = dbconfig.getConnection();
                 PreparedStatement pst = conn.prepareStatement(updateSql)) {
                pst.setInt(1, newUnit);
                pst.setDouble(2, newTotal);
                pst.setString(3, stock.getModel());
                pst.executeUpdate();
            }

            old.setUnit(newUnit);
            old.setTotalPrice(newTotal);
            return old;
        }

        // if model not found → insert new record
        String sql = "INSERT INTO inventorystock (product_name, model, price_per_quantity, unit, total_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, stock.getProductName());
            pst.setString(2, stock.getModel());
            pst.setDouble(3, stock.getPricePerQuantity());
            pst.setInt(4, stock.getUnit());
            pst.setDouble(5, stock.getPricePerQuantity() * stock.getUnit());
            pst.setString(6, stock.getStatus());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    stock.setProductId(rs.getInt(1));
                }
            }
        }
        return stock;
    }

    // Fetch all stock records
    public List<Stock> findAll() throws SQLException {
        String sql = """
            SELECT product_id, product_name, model,price_per_quantity, unit, total_price, status, 
                   created_date, updated_date 
            FROM inventorystock
        """;

        List<Stock> stockList = new ArrayList<>();
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stockList.add(mapRow(rs));
            }
        }
        return stockList;
    }

    // Fetch stock by ID
    public Optional<Stock> findById(int id) throws SQLException {
        String sql = """
            SELECT product_id, product_name, model,price_per_quantity, unit, total_price, status,
                   created_date, updated_date 
            FROM inventorystock WHERE product_id = ?
        """;

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // Update stock details
    public boolean updateStock(Stock stock) throws SQLException {
        String sql = """
            UPDATE inventorystock 
            SET product_name = ?, model= ?,price_per_quantity = ?, unit = ?, total_price = ?, status = ? 
            WHERE product_id = ?
        """;

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            double total = stock.getPricePerQuantity() * stock.getUnit();

            ps.setString(1, stock.getProductName());
            ps.setString(2, stock.getModel());
            ps.setDouble(3, stock.getPricePerQuantity());
            ps.setInt(4, stock.getUnit());
            ps.setDouble(5, total);
            ps.setString(6, stock.getStatus());
            ps.setInt(7, stock.getProductId());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete stock record by ID
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM inventorystock WHERE product_id = ?";
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
    public Optional<Stock> findByModel(String model) throws SQLException {
        String sql = "SELECT * FROM inventorystock WHERE model = ?";
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, model);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Stock stock = new Stock();
                    stock.setProductId(rs.getInt("product_id"));
                    stock.setProductName(rs.getString("product_name"));
                    stock.setModel(rs.getString("model"));
                    stock.setPricePerQuantity(rs.getDouble("price_per_quantity"));
                    stock.setUnit(rs.getInt("unit"));
                    stock.setTotalPrice(rs.getDouble("total_price"));
                    stock.setStatus(rs.getString("status"));
                    return Optional.of(stock);
                }
            }
        }
        return Optional.empty();
    }

    // Map database row → Stock object
    private Stock mapRow(ResultSet rs) throws SQLException {
        Stock stock = new Stock();
        stock.setProductId(rs.getInt("product_id"));
        stock.setProductName(rs.getString("product_name"));
        stock.setModel(rs.getString("model"));
        stock.setPricePerQuantity(rs.getDouble("price_per_quantity"));
        stock.setUnit(rs.getInt("unit"));
        stock.setTotalPrice(rs.getDouble("total_price"));
        stock.setStatus(rs.getString("status"));

        Timestamp created = rs.getTimestamp("created_date");
        Timestamp updated = rs.getTimestamp("updated_date");

        if (created != null) stock.setCreatedDate(created.toLocalDateTime());
        if (updated != null) stock.setUpdatedDate(updated.toLocalDateTime());

        return stock;
    }
}
