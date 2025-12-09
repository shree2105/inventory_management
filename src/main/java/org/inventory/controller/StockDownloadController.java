
package org.inventory.controller;

import org.inventory.model.Stock;
import org.inventory.service.StockService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock")
public class StockDownloadController {

    private final StockService stockService;

    public StockDownloadController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * Download CSV or XLSX.
     * - format = csv | xlsx (default = csv)
     * - optional id param: if present downloads only that item
     *
     * Example:
     * GET /api/stock/download?format=xlsx&id=5
     * GET /api/stock/download?format=csv
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadStockData(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) Integer id
    ) throws Exception {

        // If id provided, fetch single item; if not, fetch all
        List<Stock> stocks;
        String fileNameSuffix = "all";
        if (id != null) {
            Optional<Stock> maybe = stockService.getById(id); // adapt if your service method name differs
            if (maybe.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("Stock with id " + id + " not found").getBytes(StandardCharsets.UTF_8));
            }
            stocks = Collections.singletonList(maybe.get());
            fileNameSuffix = "id-" + id;
        } else {
            stocks = stockService.getAllItems(null, null); // adapt if your signature differs
        }

        byte[] fileContent;
        String fileName;
        MediaType mediaType;

        if ("xlsx".equalsIgnoreCase(format)) {
            fileContent = buildExcelBytes(stocks);
            fileName = "items-" + fileNameSuffix + ".xlsx";
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            fileContent = buildCsvBytes(stocks);
            fileName = "items-" + fileNameSuffix + ".csv";
            mediaType = MediaType.TEXT_PLAIN;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    // ------------------ CSV ------------------
    private byte[] buildCsvBytes(List<Stock> stocks) {
        StringBuilder sb = new StringBuilder();
        sb.append("productId,productName,model,pricePerQuantity,unit,totalPrice,status,createdDate,updatedDate\n");
        for (Stock s : stocks) {
            sb.append(quoteCsv(s.getProductId())).append(",")
                    .append(quoteCsv(s.getProductName())).append(",")
                    .append(quoteCsv(s.getModel())).append(",")
                    .append(s.getPricePerQuantity() == null ? "" : s.getPricePerQuantity()).append(",")
                    .append(s.getUnit() == null ? "" : s.getUnit()).append(",")
                    .append(s.getTotalPrice() == null ? "" : s.getTotalPrice()).append(",")
                    .append(quoteCsv(s.getStatus())).append(",")
                    .append(quoteCsv(s.getCreatedDate() == null ? "" : s.getCreatedDate().toString())).append(",")
                    .append(quoteCsv(s.getUpdatedDate() == null ? "" : s.getUpdatedDate().toString())).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Simple CSV quoting to avoid breaking on commas/newlines
    private String quoteCsv(Object obj) {
        if (obj == null) return "";
        String s = String.valueOf(obj);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\""); // escape quotes
            return "\"" + s + "\"";
        }
        return s;
    }

    // ------------------ Excel ------------------
    private byte[] buildExcelBytes(List<Stock> stocks) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Items");

            String[] columns = {"Product ID", "Product Name", "Model", "Price/Qty", "Unit", "Total", "Status", "Created", "Updated"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);

            int rowNum = 1;
            for (Stock s : stocks) {
                Row row = sheet.createRow(rowNum++);
                // Using setCellValue with safe null handling
                if (s.getProductId() != null) row.createCell(0).setCellValue(s.getProductId());
                row.createCell(1).setCellValue(s.getProductName() == null ? "" : s.getProductName());
                row.createCell(2).setCellValue(s.getModel() == null ? "" : s.getModel());
                if (s.getPricePerQuantity() != null) row.createCell(3).setCellValue(s.getPricePerQuantity());
                if (s.getUnit() != null) row.createCell(4).setCellValue(s.getUnit());
                if (s.getTotalPrice() != null) row.createCell(5).setCellValue(s.getTotalPrice());
                row.createCell(6).setCellValue(s.getStatus() == null ? "" : s.getStatus());
                row.createCell(7).setCellValue(s.getCreatedDate() == null ? "" : s.getCreatedDate().toString());
                row.createCell(8).setCellValue(s.getUpdatedDate() == null ? "" : s.getUpdatedDate().toString());
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
