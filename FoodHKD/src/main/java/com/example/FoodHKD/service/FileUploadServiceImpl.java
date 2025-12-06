package com.example.FoodHKD.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("File không được để trống");
        }

        // Tạo thư mục nếu chưa tồn tại
        Path folderPath = Paths.get(UPLOAD_DIR + folder);
        Files.createDirectories(folderPath);

        // Tạo tên file duy nhất
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new Exception("Tên file không hợp lệ");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID() + fileExtension;

        // Lưu file
        Path filepath = folderPath.resolve(uniqueFilename);
        Files.write(filepath, file.getBytes());

        // Trả về đường dẫn để lưu vào DB
        return folder + "/" + uniqueFilename;
    }

    @Override
    public void deleteFile(String filename, String folder) throws Exception {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        Path filepath = Paths.get(UPLOAD_DIR + folder + "/" + filename);
        if (Files.exists(filepath)) {
            Files.delete(filepath);
        }
    }
}
