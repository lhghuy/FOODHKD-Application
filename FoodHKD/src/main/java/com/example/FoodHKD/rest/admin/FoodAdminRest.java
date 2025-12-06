package com.example.FoodHKD.rest.admin;

import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.service.CategoryService;
import com.example.FoodHKD.service.FoodItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/foods")
public class FoodAdminRest {
    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private CategoryService categoryService;

    // Sử dụng absolute path để tránh issues với relative path
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/FoodHKD_uploads/foods/";

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFoodItems() {
        try {
            List<FoodItem> foodItems = foodItemService.getAllFoodItems();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", foodItems);
            response.put("total", foodItems.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch food items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFoodItemById(@PathVariable("id") Integer id) {
        try {
            FoodItem foodItem = foodItemService.getFoodItemById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", foodItem);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Food item not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch food item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFoodItem(@RequestBody @Valid FoodItem foodItem,
            BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            response.put("errors", getValidationErrors(result));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            FoodItem createdFoodItem = foodItemService.createFoodItem(foodItem);
            response.put("success", true);
            response.put("message", "Food item created successfully");
            response.put("data", createdFoodItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create food item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFoodItem(@PathVariable("id") Integer id,
            @RequestBody @Valid FoodItem foodItem,
            BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            response.put("errors", getValidationErrors(result));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            FoodItem updatedFoodItem = foodItemService.updateFoodItem(id, foodItem);
            response.put("success", true);
            response.put("message", "Food item updated successfully");
            response.put("data", updatedFoodItem);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Food item not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update food item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFoodItem(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            foodItemService.deleteFoodItem(id);
            response.put("success", true);
            response.put("message", "Food item deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete food item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categoryService.getAllCategories());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/admin/foods/image/{filename} - Serve hình ảnh từ uploads folder
     * Giải pháp cho ngrok: Frontend lấy ảnh qua endpoint này thay vì /uploads/foods/...
     * Cách dùng: https://ngrok-url/api/admin/foods/image/uuid-filename.jpg
     */
    @GetMapping("/image/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            // Validate filename để tránh path traversal attack
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                System.out.println("❌ Invalid filename (path traversal attempt): " + filename);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Sử dụng absolute path giống như upload
            String uploadDir = System.getProperty("user.home") + "/FoodHKD_uploads/foods/";
            Path filePath = Paths.get(uploadDir).resolve(filename);
            
            System.out.println("Looking for image at: " + filePath.toAbsolutePath());
            
            // Kiểm tra file tồn tại
            if (!Files.exists(filePath)) {
                System.out.println("❌ Image not found: " + filePath.toAbsolutePath());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Đọc file
            byte[] imageBytes = Files.readAllBytes(filePath);
            System.out.println("✅ Image served successfully: " + filename + " (" + imageBytes.length + " bytes)");
            
            // Detect content type từ filename
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(imageBytes.length)
                    .body(imageBytes);
                    
        } catch (IOException e) {
            System.err.println("❌ Error reading image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Detect content type từ file extension
     */
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    /**
     * POST /api/admin/foods/upload-image - Upload hình ảnh sản phẩm
     * Request: multipart/form-data với file
     * Response: { "success": true, "imageUrl": "/uploads/foods/...", "imagePath": "..." }
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadFoodImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== DEBUG UPLOAD IMAGE ===");
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());
            
            // Kiểm tra file không rỗng
            if (file.isEmpty()) {
                System.out.println("ERROR: File is empty");
                response.put("success", false);
                response.put("message", "File không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra loại file dựa trên extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                System.out.println("ERROR: Original filename is null");
                response.put("success", false);
                response.put("message", "Không thể xác định tên file");
                return ResponseEntity.badRequest().body(response);
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            System.out.println("File extension: " + fileExtension);
            
            String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
            boolean isValidExtension = false;
            for (String ext : allowedExtensions) {
                if (fileExtension.equals(ext)) {
                    isValidExtension = true;
                    break;
                }
            }

            if (!isValidExtension) {
                System.out.println("ERROR: Invalid extension - " + fileExtension);
                response.put("success", false);
                response.put("message", "Chỉ chấp nhận file hình ảnh (jpg, jpeg, png, gif, bmp, webp)");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra contentType nếu có (fallback)
            String contentType = file.getContentType();
            System.out.println("Validating contentType: " + contentType);
            // Cho phép null contentType vì Flutter có thể không gửi
            if (contentType != null && !contentType.startsWith("image/") && !contentType.equals("application/octet-stream")) {
                System.out.println("ERROR: Invalid content type - " + contentType);
                response.put("success", false);
                response.put("message", "File không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }
            System.out.println("✅ File validation passed");

            // Kiểm tra kích thước (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                System.out.println("ERROR: File size exceeds 5MB");
                response.put("success", false);
                response.put("message", "Kích thước file không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating upload directory: " + uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String filename = UUID.randomUUID().toString() + "." + fileExtension;
            Path filePath = uploadPath.resolve(filename);
            System.out.println("Saving file to: " + filePath.toAbsolutePath());

            // Lưu file
            Files.write(filePath, file.getBytes());
            System.out.println("✅ File saved successfully");

            // Đường dẫn URL trả về - dùng endpoint GET /api/admin/foods/image/{filename} để serve ảnh
            // Cách này hoạt động qua ngrok tunnel
            String imageUrl = "/api/admin/foods/image/" + filename;

            // Response thành công
            response.put("success", true);
            response.put("message", "Upload hình ảnh thành công");
            response.put("imageUrl", imageUrl);
            response.put("imagePath", filePath.toString());
            response.put("filename", filename);
            System.out.println("Response: " + response);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            System.err.println("ERROR IOException: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            System.err.println("ERROR Exception: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Lỗi upload hình: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Map<String, String> getValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }
}
