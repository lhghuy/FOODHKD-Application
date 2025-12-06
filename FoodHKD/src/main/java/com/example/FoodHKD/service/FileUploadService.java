package com.example.FoodHKD.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String uploadFile(MultipartFile file, String folder) throws Exception;
    
    void deleteFile(String filename, String folder) throws Exception;
}
