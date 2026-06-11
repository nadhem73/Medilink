package com.medilinktunisia.laboratoryservice.service;

import com.medilinktunisia.laboratoryservice.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service de gestion des uploads de fichiers
 * Selon cahier des charges - Upload des résultats au format PDF (max 10MB)
 */
@Service
@Slf4j
public class FileUploadService {

    @Value("${file.upload.directory:uploads/results}")
    private String uploadDirectory;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".pdf"};

    /**
     * Upload d'un fichier PDF de résultat
     */
    public String uploadPdfResult(MultipartFile file) {
        log.info("Uploading PDF file: {}", file.getOriginalFilename());

        // Validation du fichier
        validateFile(file);

        // Créer le répertoire d'upload s'il n'existe pas
        createUploadDirectory();

        // Générer un nom de fichier unique
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path filePath = Paths.get(uploadDirectory, fileName);

        try {
            // Copier le fichier vers le répertoire d'upload
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", fileName);
            return filePath.toString();
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new FileUploadException("Échec de l'upload du fichier", e);
        }
    }

    /**
     * Supprimer un fichier
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("File deleted successfully: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            // Ne pas lancer d'exception pour ne pas bloquer la suppression de l'entité
        }
    }

    /**
     * Valider le fichier
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Le fichier est vide");
        }

        // Vérifier la taille du fichier (max 10MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("La taille du fichier dépasse la limite de 10MB");
        }

        // Vérifier l'extension du fichier
        String fileName = file.getOriginalFilename();
        if (fileName == null || !isPdfFile(fileName)) {
            throw new FileUploadException("Seuls les fichiers PDF sont autorisés");
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new FileUploadException("Le type de fichier n'est pas valide. Seuls les fichiers PDF sont autorisés");
        }
    }

    /**
     * Vérifier si le fichier est un PDF
     */
    private boolean isPdfFile(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerCaseFileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Créer le répertoire d'upload s'il n'existe pas
     */
    private void createUploadDirectory() {
        try {
            Path path = Paths.get(uploadDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Upload directory created: {}", uploadDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", e.getMessage());
            throw new FileUploadException("Échec de la création du répertoire d'upload", e);
        }
    }

    /**
     * Générer un nom de fichier unique
     */
    private String generateUniqueFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        return String.format("result_%s_%s%s", timestamp, uuid, extension);
    }

    /**
     * Obtenir l'extension du fichier
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
}
