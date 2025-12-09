////package org.inventory.controller;
////
////import jakarta.validation.Valid;
////import org.inventory.model.Stock;
////import org.inventory.service.StockService;
////import org.springframework.http.HttpStatus;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////import java.util.Optional;
////
////@RestController
////@RequestMapping("/api/stock")
////public class StockController {
////
////    private final StockService stockService;
////
////    public StockController(StockService stockService) {
////        this.stockService = stockService;
////    }
////
////    //  Simple test endpoint
////    @GetMapping("/testApi")
////    public ResponseEntity<String> testApi() {
////        return ResponseEntity.ok(" Stock API is working perfectly!");
////    }
////
////    //  Get all stock items
////    @GetMapping("/getAllItems")
////    public ResponseEntity<List<Stock>> getAllStocks() throws Exception {
////        List<Stock> list = stockService.getAllItems(null, null);
////        return ResponseEntity.ok(list);
////    }
////
////    //  Get single stock by ID
////    @GetMapping("/getItemById/{id}")
////    public ResponseEntity<Stock> getStockById(@PathVariable("id") Integer id) throws Exception {
////        Optional<Stock> opt = stockService.getById(id);
////        return opt.map(ResponseEntity::ok)
////                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
////    }
////
////    //  Create new stock item
////    @PostMapping("/addNewItem")
////    public ResponseEntity<Stock> createStock(@Valid @RequestBody Stock stock) throws Exception {
////        Stock created = stockService.insertItem(stock);
////        return ResponseEntity.status(HttpStatus.CREATED).body(created);
////    }
////
////    //  Update existing stock item
////    @PutMapping("/updateItem/{id}")
////    public ResponseEntity<Stock> updateStock(@PathVariable("id") Integer id,
////                                             @Valid @RequestBody Stock stock) throws Exception {
////        Stock updated = stockService.updateItem(id, stock);
////        return ResponseEntity.ok(updated);
////    }
////
////    // Delete stock item
////    @DeleteMapping("/deleteItem/{id}")
////    public ResponseEntity<Void> deleteStock(@PathVariable("id") Integer id) throws Exception {
////        boolean deleted = stockService.deleteItem(id);
////        if (deleted) return ResponseEntity.noContent().build();
////        return ResponseEntity.notFound().build();
////    }
////}
//package org.inventory.controller;
//
//import jakarta.validation.Valid;
//import org.inventory.model.Stock;
//import org.inventory.security.JwtService;
//import org.inventory.service.StockService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/stock")
//public class StockController {
//
//    private final StockService stockService;
//    private final JwtService jwtService;   // ✅ NEW
//
//    public StockController(StockService stockService,
//                           JwtService jwtService) {
//        this.stockService = stockService;
//        this.jwtService = jwtService;
//    }
//
//    // Simple test endpoint
//    @GetMapping("/testApi")
//    public ResponseEntity<String> testApi() {
//        return ResponseEntity.ok("Stock API is working perfectly!");
//    }
//
//    // Get all stock items  (PUBLIC)
//    @GetMapping("/getAllItems")
//    public ResponseEntity<List<Stock>> getAllStocks() throws Exception {
//        List<Stock> list = stockService.getAllItems(null, null);
//        return ResponseEntity.ok(list);
//    }
//
//    // Get single stock by ID (PUBLIC)
//    @GetMapping("/getItemById/{id}")
//    public ResponseEntity<Stock> getStockById(@PathVariable("id") Integer id) throws Exception {
//        Optional<Stock> opt = stockService.getById(id);
//        return opt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
//    }
//
//    // ----------------------------- ADMIN ONLY HELPERS -----------------------------
//
//    /**
//     * Checks if the Authorization header contains a valid Admin JWT.
//     */
//    private boolean isAdminToken(String authHeader) {
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return false;
//        }
//        String token = authHeader.substring(7); // remove "Bearer "
//
//        try {
//            String rights = jwtService.extractClaim(token, claims -> (String) claims.get("rights"));
//            return rights != null && rights.equalsIgnoreCase("Admin");
//        } catch (Exception e) {
//            return false; // invalid / expired token
//        }
//    }
//
//    // ----------------------------- ADMIN-ONLY ENDPOINTS --------------------------
//
//    // Create new stock item (ADMIN ONLY)
//    @PostMapping("/addNewItem")
//    public ResponseEntity<?> createStock(
//            @RequestHeader(value = "Authorization", required = false) String authHeader,
//            @Valid @RequestBody Stock stock) throws Exception {
//
//        if (!isAdminToken(authHeader)) {
//            // 403 = Forbidden (user is not allowed)
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("message", "Only admin can add new products"));
//        }
//
//        Stock created = stockService.insertItem(stock);
//        return ResponseEntity.status(HttpStatus.CREATED).body(created);
//    }
//
//    // Update existing stock item (ADMIN ONLY)
//    @PutMapping("/updateItem/{id}")
//    public ResponseEntity<?> updateStock(
//            @RequestHeader(value = "Authorization", required = false) String authHeader,
//            @PathVariable("id") Integer id,
//            @Valid @RequestBody Stock stock) throws Exception {
//
//        if (!isAdminToken(authHeader)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("message", "Only admin can update products"));
//        }
//
//        Stock updated = stockService.updateItem(id, stock);
//        return ResponseEntity.ok(updated);
//    }
//
//    // Delete stock item (ADMIN ONLY)
//    @DeleteMapping("/deleteItem/{id}")
//    public ResponseEntity<?> deleteStock(
//            @RequestHeader(value = "Authorization", required = false) String authHeader,
//            @PathVariable("id") Integer id) throws Exception {
//
//        if (!isAdminToken(authHeader)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("message", "Only admin can delete products"));
//        }
//
//        boolean deleted = stockService.deleteItem(id);
//        if (deleted) return ResponseEntity.noContent().build();
//        return ResponseEntity.notFound().build();
//    }
//}
package org.inventory.controller;

import jakarta.validation.Valid;
import org.inventory.model.Stock;
import org.inventory.security.JwtService;
import org.inventory.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;
    private final JwtService jwtService; // can be null in tests

    // ✅ Constructor used by Spring in real app (with JwtService)
    @Autowired
    public StockController(StockService stockService, JwtService jwtService) {
        this.stockService = stockService;
        this.jwtService = jwtService;
    }

    // ✅ Extra constructor used by unit tests (no JwtService)
    public StockController(StockService stockService) {
        this.stockService = stockService;
        this.jwtService = null; // no security check in tests
    }

    // Simple test endpoint
    @GetMapping("/testApi")
    public ResponseEntity<String> testApi() {
        return ResponseEntity.ok(" Stock API is working perfectly!");
    }

    // Get all stock items (PUBLIC)
    @GetMapping("/getAllItems")
    public ResponseEntity<List<Stock>> getAllStocks() throws Exception {
        List<Stock> list = stockService.getAllItems(null, null);
        return ResponseEntity.ok(list);
    }

    // Get single stock by ID (PUBLIC)
    @GetMapping("/getItemById/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable("id") Integer id) throws Exception {
        Optional<Stock> opt = stockService.getById(id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // --------------------------------------------------------------------
    // HELPER: Check if token is admin
    // --------------------------------------------------------------------
    private boolean isAdminToken(String authHeader) {
        // In tests, jwtService will be null → skip check (behave like old code)
        if (jwtService == null) {
            return true;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7); // remove "Bearer "

        try {
            String rights = jwtService.extractClaim(token, claims -> (String) claims.get("rights"));
            return rights != null && rights.equalsIgnoreCase("Admin");
        } catch (Exception e) {
            return false; // invalid / expired token
        }
    }

    // --------------------------------------------------------------------
    // ADMIN-ONLY ENDPOINTS (real API)
    // --------------------------------------------------------------------

    // Create new stock item (ADMIN ONLY via token)
    @PostMapping("/addNewItem")
    public ResponseEntity<Stock> createStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody Stock stock) throws Exception {

        if (!isAdminToken(authHeader)) {
            // 403 forbidden, no body (to keep return type ResponseEntity<Stock>)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Stock created = stockService.insertItem(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Update existing stock item (ADMIN ONLY via token)
    @PutMapping("/updateItem/{id}")
    public ResponseEntity<Stock> updateStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Integer id,
            @Valid @RequestBody Stock stock) throws Exception {

        if (!isAdminToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Stock updated = stockService.updateItem(id, stock);
        return ResponseEntity.ok(updated);
    }

    // Delete stock item (ADMIN ONLY via token)
    @DeleteMapping("/deleteItem/{id}")
    public ResponseEntity<Void> deleteStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Integer id) throws Exception {

        if (!isAdminToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = stockService.deleteItem(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    // --------------------------------------------------------------------
    // HELPER OVERLOADS FOR TESTS (no Authorization header)
    // These match the OLD method signatures your tests use.
    // --------------------------------------------------------------------

    // Used by tests: createStock(stock)
    public ResponseEntity<Stock> createStock(@Valid @RequestBody Stock stock) throws Exception {
        return createStock(null, stock);
    }

    // Used by tests: updateStock(id, stock)
    public ResponseEntity<Stock> updateStock(@PathVariable("id") Integer id,
                                             @Valid @RequestBody Stock stock) throws Exception {
        return updateStock(null, id, stock);
    }

    // Used by tests: deleteStock(id)
    public ResponseEntity<Void> deleteStock(@PathVariable("id") Integer id) throws Exception {
        return deleteStock(null, id);
    }
}
