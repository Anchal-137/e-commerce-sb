package com.example.ecommerce.service;

import com.example.ecommerce.dto.file.FileUploadResponse;
import com.example.ecommerce.exception.BadRequestException;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path fileStoragePath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @PostConstruct
    public void init() {
        this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStoragePath);
            log.info("File storage directory created: {}", this.fileStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public FileUploadResponse storeFile(MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);

        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Generate unique filename
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Check for invalid characters
            if (newFileName.contains("..")) {
                throw new BadRequestException("Invalid path sequence in filename");
            }

            Path targetLocation = this.fileStoragePath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", newFileName);

            return FileUploadResponse.builder()
                    .fileName(newFileName)
                    .fileUrl("/api/files/" + newFileName)
                    .fileType(file.getContentType())
                    .size(file.getSize())
                    .message("File uploaded successfully")
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + originalFileName, e);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("File not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("File not found: " + fileName);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileName);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + fileName, e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
