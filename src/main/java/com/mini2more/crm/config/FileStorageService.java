/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.config;

import com.mini2more.crm.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService(@Value("${file.storage.location}") String storagePath) {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storageLocation.resolve("documents"));
            Files.createDirectories(storageLocation.resolve("images"));
            Files.createDirectories(storageLocation.resolve("invoices"));
            Files.createDirectories(storageLocation.resolve("boq"));
            log.info("File storage initialized at: {}", storageLocation);
        } catch (IOException e) {
            throw new BusinessException("Could not create storage directory: " + e.getMessage());
        }
    }

    /**
     * Store a file in the specified subdirectory.
     * 
     * @param file         the uploaded file
     * @param subDirectory subdirectory (documents, images, invoices, boq)
     * @return the stored filename (UUID-based)
     */
    public String storeFile(MultipartFile file, String subDirectory) {
        String originalFilename = StringUtils
                .cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

        if (originalFilename.contains("..")) {
            throw new BusinessException("Invalid file path: " + originalFilename);
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID().toString() + extension;
        Path targetLocation = storageLocation.resolve(subDirectory).resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {} as {}", originalFilename, storedFilename);
            return subDirectory + "/" + storedFilename;
        } catch (IOException e) {
            throw new BusinessException("Failed to store file: " + e.getMessage());
        }
    }

    /**
     * Load a file as a Resource.
     * 
     * @param filePath relative path within storage (e.g., "documents/uuid.pdf")
     * @return the file resource
     */
    public Resource loadFile(String filePath) {
        try {
            Path file = storageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BusinessException("File not found: " + filePath);
        } catch (MalformedURLException e) {
            throw new BusinessException("File not found: " + filePath);
        }
    }

    /**
     * Delete a file from storage.
     * 
     * @param filePath relative path within storage
     */
    public void deleteFile(String filePath) {
        try {
            Path file = storageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath);
        }
    }

    public Path getStorageLocation() {
        return storageLocation;
    }
}
