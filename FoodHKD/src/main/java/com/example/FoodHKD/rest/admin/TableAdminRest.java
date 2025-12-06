package com.example.FoodHKD.rest.admin;

import com.example.FoodHKD.model.TableDetail;
import com.example.FoodHKD.model.TableEntity;
import com.example.FoodHKD.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/tables")
public class TableAdminRest {

    @Autowired
    private TableService tableService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTables() {
        try {
            List<TableEntity> tables = tableService.getAllTables();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tables);
            response.put("total", tables.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch tables: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTableById(@PathVariable("id") Integer id) {
        try {
            Optional<TableEntity> tableOptional = tableService.getTableById(id);
            if (tableOptional.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", tableOptional.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Table not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch table: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getTableDetails(@PathVariable("id") Integer id) {
        try {
            Optional<TableEntity> tableOptional = tableService.getTableById(id);
            if (tableOptional.isPresent()) {
                TableEntity table = tableOptional.get();
                List<TableDetail> tableDetails = tableService.getTableDetailsByTable(table);
                BigDecimal totalPrice = tableDetails.stream()
                        .map(TableDetail::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);

                Map<String, Object> data = new HashMap<>();
                data.put("table", table);
                data.put("tableDetails", tableDetails);
                data.put("totalPrice", totalPrice);
                data.put("itemCount", tableDetails.size());

                response.put("data", data);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Table not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch table details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTable(@RequestBody @Valid TableEntity table,
            BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            response.put("errors", getValidationErrors(result));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Set default status if not provided
            if (table.getStatus() == null || table.getStatus().isEmpty()) {
                table.setStatus("Trong");
            }
            TableEntity createdTable = tableService.saveTable(table);
            response.put("success", true);
            response.put("message", "Table created successfully");
            response.put("data", createdTable);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create table: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // PUT /api/admin/tables/{id} - Update table
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTable(@PathVariable("id") Integer id,
            @RequestBody @Valid TableEntity table,
            BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            response.put("errors", getValidationErrors(result));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Optional<TableEntity> existingTableOptional = tableService.getTableById(id);
            if (existingTableOptional.isPresent()) {
                TableEntity existingTable = existingTableOptional.get();

                // Update fields
                existingTable.setTableNumber(table.getTableNumber());
                if (table.getStatus() != null) {
                    existingTable.setStatus(table.getStatus());
                }
                if (table.getEmployee() != null) {
                    existingTable.setEmployee(table.getEmployee());
                }

                TableEntity updatedTable = tableService.saveTable(existingTable);
                response.put("success", true);
                response.put("message", "Table updated successfully");
                response.put("data", updatedTable);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Table not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update table: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DELETE /api/admin/tables/{id} - Delete table
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTable(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<TableEntity> existingTable = tableService.getTableById(id);
            if (existingTable.isPresent()) {
                tableService.deleteTable(id);
                response.put("success", true);
                response.put("message", "Table deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Table not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete table: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // PATCH /api/admin/tables/{id}/status - Update table status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateTableStatus(@PathVariable("id") Integer id,
            @RequestBody Map<String, String> statusData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String status = statusData.get("status");
            if (status == null || status.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Status is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Optional<TableEntity> tableOptional = tableService.getTableById(id);
            if (tableOptional.isPresent()) {
                TableEntity table = tableOptional.get();
                tableService.updateTableStatus(table, status);

                // Get updated table
                Optional<TableEntity> updatedTableOptional = tableService.getTableById(id);
                response.put("success", true);
                response.put("message", "Table status updated successfully");
                response.put("data", updatedTableOptional.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Table not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update table status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to extract validation errors
    private Map<String, String> getValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }
}
