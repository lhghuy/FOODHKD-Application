package com.example.FoodHKD.rest.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Invoice;
import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.repository.InvoiceRepository;

/**
 * REST Controller for Admin Invoice Management
 * Endpoints: /api/admin/invoices
 * Only retrieves invoices for orders that have been served (customer received their food)
 */
@RestController
@RequestMapping("/api/admin/invoices")
public class InvoiceAdminRest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * GET /api/admin/invoices - Get all invoices for served orders
     * Response: { "success": true, "invoices": [...], "total": N }
     */
    @GetMapping
    public ResponseEntity<?> getAllInvoices() {
        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            List<Map<String, Object>> invoicesList = new ArrayList<>();

            for (Invoice invoice : invoices) {
                // Only include invoices for orders that have been served
                if (invoice.getOrder() != null && "Served".equals(invoice.getOrder().getStatus())) {
                    invoicesList.add(convertInvoiceToMap(invoice));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("invoices", invoicesList);
            response.put("total", invoicesList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/invoices/filter?dateFrom=YYYY-MM-DD&dateTo=YYYY-MM-DD - Filter invoices by date range
     * Response: { "success": true, "invoices": [...], "total": N }
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getInvoicesByDateRange(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            List<Map<String, Object>> filteredInvoices = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom, formatter) : null;
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo, formatter) : null;

            for (Invoice invoice : invoices) {
                // Only include invoices for orders that have been served
                if (invoice.getOrder() == null || !"Served".equals(invoice.getOrder().getStatus())) {
                    continue;
                }

                LocalDate invoiceDate = invoice.getIssuedAt().toLocalDate();

                boolean inRange = true;
                if (fromDate != null && invoiceDate.isBefore(fromDate)) {
                    inRange = false;
                }
                if (toDate != null && invoiceDate.isAfter(toDate)) {
                    inRange = false;
                }

                if (inRange) {
                    filteredInvoices.add(convertInvoiceToMap(invoice));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("invoices", filteredInvoices);
            response.put("total", filteredInvoices.size());
            response.put("dateFrom", dateFrom);
            response.put("dateTo", dateTo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/invoices/{id} - Delete invoice
     * Response: { "success": true, "message": "Invoice deleted successfully" }
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Integer id) {
        try {
            if (!invoiceRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Invoice not found"));
            }

            invoiceRepository.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invoice deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Convert Invoice entity to Map
     */
    private Map<String, Object> convertInvoiceToMap(Invoice invoice) {
        Map<String, Object> map = new HashMap<>();
        map.put("invoiceID", invoice.getInvoiceID());
        map.put("totalAmount", invoice.getTotalAmount());
        map.put("issuedAt", invoice.getIssuedAt());

        if (invoice.getOrder() != null) {
            Order order = invoice.getOrder();
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderID", order.getOrderID());
            orderMap.put("fullName", order.getFullName());
            orderMap.put("phone", order.getPhone());
            orderMap.put("status", order.getStatus());
            orderMap.put("createdAt", order.getCreatedAt());

            map.put("order", orderMap);
            map.put("customerName", order.getFullName());
            map.put("orderId", order.getOrderID());
        }

        if (invoice.getIssuedBy() != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userID", invoice.getIssuedBy().getUserID());
            userMap.put("username", invoice.getIssuedBy().getUsername());
            userMap.put("fullName", invoice.getIssuedBy().getFullName());
            map.put("issuedBy", userMap);
        }

        return map;
    }
}