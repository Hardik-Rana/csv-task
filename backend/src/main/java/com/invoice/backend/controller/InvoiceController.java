package com.invoice.backend.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.invoice.backend.model.Invoice;
import com.invoice.backend.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadCSV(@RequestParam("file") MultipartFile file) {
        List<Invoice> saved = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // skip header
                }

                String[] fields = line.split(",");

                if (fields.length != 5) {
                    errors.add("Invalid row (wrong number of fields): " + line);
                    continue;
                }

                try {
                    String customerId = fields[0].trim();
                    String invoiceNumber = fields[1].trim();
                    LocalDate invoiceDate = LocalDate.parse(fields[2].trim(), dateFormatter);
                    String description = fields[3].trim();
                    double amount = Double.parseDouble(fields[4].trim());

                    Invoice invoice = new Invoice(customerId, invoiceNumber, invoiceDate, description, amount);
                    Invoice savedInvoice = invoiceService.saveInvoice(invoice);

                    if (savedInvoice != null) {
                        saved.add(savedInvoice);
                    } else {
                        errors.add("Duplicate entry: " + line);
                    }

                } catch (Exception ex) {
                    errors.add("Parse error: " + line);
                }
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to read file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        if (!errors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("savedCount", saved.size());
            errorResponse.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "success");
        successResponse.put("savedCount", saved.size());
        return ResponseEntity.ok(successResponse);
    }

    @GetMapping
    public List<Map<String, Object>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Invoice invoice : invoices) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", invoice.getId());
            map.put("customerId", invoice.getCustomerId());
            map.put("invoiceNumber", invoice.getInvoiceNumber());
            map.put("invoiceDate", invoice.getInvoiceDate());
            map.put("description", invoice.getDescription());
            map.put("amount", invoice.getAmount());

            long age = java.time.temporal.ChronoUnit.DAYS.between(invoice.getInvoiceDate(), LocalDate.now());
            map.put("invoiceAge", age);

            result.add(map);
        }

        return result;
    }
}
