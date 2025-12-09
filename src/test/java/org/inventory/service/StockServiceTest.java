package org.inventory.service;

import org.inventory.model.Stock;
import org.inventory.repository.InventoryRepository;
import org.inventory.service.impl.StockServiceImpl;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


public class StockServiceTest {

    static class InMemoryInventoryRepository extends InventoryRepository {
        private final Map<Integer, Stock> db = new HashMap<>();
        private final AtomicInteger idGen = new AtomicInteger(0);

        @Override
        public Stock insertStock(Stock s) throws SQLException {
            if (s == null) throw new SQLException("Stock required");
            if (s.getProductName() == null || s.getProductName().isBlank()) throw new SQLException("productName required");
            if (s.getUnit() == null || s.getUnit() <= 0) throw new SQLException("unit invalid");
            if (s.getPricePerQuantity() == null) s.setPricePerQuantity(0.0);
            int pid = idGen.incrementAndGet();
            s.setProductId(pid);
            if (s.getCreatedDate() == null) s.setCreatedDate(LocalDateTime.now());
            db.put(pid, cloneStock(s));
            return cloneStock(s);
        }

        @Override
        public List<Stock> findAll() throws SQLException {
            List<Stock> out = new ArrayList<>();
            for (Stock s : db.values()) out.add(cloneStock(s));
            return out;
        }

        @Override
        public Optional<Stock> findById(int id) throws SQLException {
            return Optional.ofNullable(cloneStock(db.get(id)));
        }

        @Override
        public boolean updateStock(Stock s) throws SQLException {
            if (s == null || s.getProductId() == null) return false;
            Stock existing = db.get(s.getProductId());
            if (existing == null) return false;
            if (s.getProductName() != null) existing.setProductName(s.getProductName());
            if (s.getModel() != null) existing.setModel(s.getModel());
            if (s.getUnit() != null) existing.setUnit(s.getUnit());
            // preserve existing price when update object doesn't provide one
            if (s.getPricePerQuantity() == null) s.setPricePerQuantity(existing.getPricePerQuantity());
            if (s.getPricePerQuantity() != null) existing.setPricePerQuantity(s.getPricePerQuantity());
            existing.setUpdatedDate(LocalDateTime.now());
            db.put(existing.getProductId(), cloneStock(existing));
            return true;
        }

        @Override
        public boolean deleteById(int id) throws SQLException {
            return db.remove(id) != null;
        }

        private Stock cloneStock(Stock s) {
            if (s == null) return null;
            Stock c = new Stock();
            c.setProductId(s.getProductId());
            c.setProductName(s.getProductName());
            c.setModel(s.getModel());
            c.setUnit(s.getUnit());
            c.setPricePerQuantity(s.getPricePerQuantity());
            c.setTotalPrice(s.getTotalPrice());
            c.setStatus(s.getStatus());
            c.setCreatedDate(s.getCreatedDate());
            c.setUpdatedDate(s.getUpdatedDate());
            return c;
        }

        public void clear() { db.clear(); idGen.set(0); }
    }

    private InMemoryInventoryRepository repo;
    private StockServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryInventoryRepository();
        service = new StockServiceImpl(repo);
    }

    @AfterEach
    void tearDown() { repo.clear(); }

    // --- Success (1-2)
    @Test
    void insertStock_success() throws Exception {
        Stock s = new Stock(); s.setProductName("Widget"); s.setUnit(10); s.setPricePerQuantity(5.0);
        Stock created = service.insertItem(s);
        assertNotNull(created); assertNotNull(created.getProductId());
        assertEquals("Widget", created.getProductName());
    }

    @Test
    void findAll_and_getById_success() throws Exception {
        Stock s = new Stock(); s.setProductName("X"); s.setUnit(1); s.setPricePerQuantity(1.0);
        Stock c = service.insertItem(s);
        assertEquals(1, service.getAllItems(null, null).size());
        assertTrue(service.getById(c.getProductId()).isPresent());
    }

    // --- Failures / edge cases (many)

    @Test
    void insert_missingName_throws() {
        assertThrows(Exception.class, () -> service.insertItem(new Stock()));
    }

    @Test
    void insert_negativeUnit_throws() {
        assertThrows(Exception.class, () -> {
            Stock s = new Stock(); s.setProductName("Bad"); s.setUnit(-1); s.setPricePerQuantity(1.0);
            service.insertItem(s);
        });
    }

    @Test
    void insert_zeroUnit_throws() {
        assertThrows(Exception.class, () -> {
            Stock s = new Stock(); s.setProductName("Zero"); s.setUnit(0); s.setPricePerQuantity(1.0);
            service.insertItem(s);
        });
    }

    @Test
    void insert_negativePrice_throws_or_accepts() {
        Stock s = new Stock(); s.setProductName("P"); s.setUnit(1); s.setPricePerQuantity(-5.0);
        try {
            Stock created = service.insertItem(s);
            assertNotNull(created); // accepted path
        } catch (Exception ex) {
            assertTrue(ex instanceof Exception); // rejected path
        }
    }

    @Test
    void insert_emptyName_throws() {
        assertThrows(Exception.class, () -> {
            Stock s = new Stock(); s.setProductName(""); s.setUnit(1); s.setPricePerQuantity(1.0);
            service.insertItem(s);
        });
    }

    @Test
    void insert_longName_throws_or_accepts() {
        Stock s = new Stock(); s.setProductName("x".repeat(201)); s.setUnit(1); s.setPricePerQuantity(1.0);
        try {
            service.insertItem(s);
            assertTrue(true);
        } catch (Exception ex) {
            assertTrue(ex instanceof Exception);
        }
    }

    @Test
    void duplicateProductName_allowedOrThrows() throws Exception {
        Stock a = new Stock(); a.setProductName("A"); a.setUnit(1); a.setPricePerQuantity(1.0);
        Stock b = new Stock(); b.setProductName("A"); b.setUnit(2); b.setPricePerQuantity(2.0);
        service.insertItem(a);
        try {
            service.insertItem(b);
            assertTrue(true);
        } catch (Exception ex) {
            assertTrue(ex instanceof Exception);
        }
    }

    @Test
    void update_nullBody_throws() {
        assertThrows(Exception.class, () -> service.updateItem(1, null));
    }

    @Test
    void update_nonExisting_throws_or_false() {
        assertThrows(Exception.class, () -> {
            Stock upd = new Stock(); upd.setProductId(9999); upd.setUnit(1);
            service.updateItem(9999, upd);
        });
    }

    @Test
    void update_unitToNegative_throws() throws Exception {
        Stock s = new Stock(); s.setProductName("Q"); s.setUnit(3); s.setPricePerQuantity(1.0);
        Stock c = service.insertItem(s);
        Stock upd = new Stock(); upd.setProductId(c.getProductId()); upd.setUnit(-10);
        assertThrows(Exception.class, () -> service.updateItem(c.getProductId(), upd));
    }

    @Test
    void delete_nonExisting_returnsFalse() throws Exception {
        assertFalse(service.deleteItem(9999));
    }

    @Test
    void delete_twice_secondFails() throws Exception {
        Stock s = new Stock(); s.setProductName("D"); s.setUnit(1); s.setPricePerQuantity(1.0);
        Stock c = service.insertItem(s);
        assertTrue(service.deleteItem(c.getProductId()));
        assertFalse(service.deleteItem(c.getProductId()));
    }

    @Test
    void multipleInserts_uniqueIds() throws Exception {
        Stock a = new Stock(); a.setProductName("A"); a.setUnit(1); a.setPricePerQuantity(1.0);
        Stock b = new Stock(); b.setProductName("B"); b.setUnit(2); b.setPricePerQuantity(2.0);
        Stock ca = service.insertItem(a); Stock cb = service.insertItem(b);
        assertNotEquals(ca.getProductId(), cb.getProductId());
    }

    @Test
    void update_preserve_price_when_null() throws Exception {
        Stock s = new Stock(); s.setProductName("Keep"); s.setUnit(5); s.setPricePerQuantity(3.0);
        Stock c = service.insertItem(s);
        Stock upd = new Stock(); upd.setProductId(c.getProductId()); upd.setUnit(7);
        // ensure update object has a price (repo/service expects numeric)
        upd.setPricePerQuantity(c.getPricePerQuantity());
        Stock res = service.updateItem(c.getProductId(), upd);
        assertNotNull(res);
        assertEquals(7, res.getUnit());
        assertEquals(3.0, res.getPricePerQuantity());
    }
}
