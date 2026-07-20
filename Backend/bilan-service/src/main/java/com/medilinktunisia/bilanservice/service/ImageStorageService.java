package com.medilinktunisia.bilanservice.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {

    @Value("${app.image-storage.path}")
    private String storagePath;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(storagePath));
            log.info("Image storage directory created at {}", storagePath);
        } catch (IOException e) {
            log.error("Could not create storage directory: {}", storagePath, e);
        }
    }

    public String saveImage(MultipartFile file) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = Paths.get(storagePath, filename);
        try {
            Files.copy(file.getInputStream(), targetPath);
            log.info("Image saved to {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Failed to save image: {}", filename, e);
            throw new RuntimeException("Failed to save image", e);
        }
    }

    public String toBase64(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.error("Failed to convert image to base64", e);
            throw new RuntimeException("Failed to convert image to base64", e);
        }
    }

    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get(imagePath));
            log.info("Deleted image: {}", imagePath);
        } catch (IOException e) {
            log.warn("Failed to delete image: {}", imagePath, e);
        }
    }
}
