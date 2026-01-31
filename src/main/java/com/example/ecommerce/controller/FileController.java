package com.example.ecommerce.controller;

import com.example.ecommerce.dto.file.FileUploadResponse;
import com.example.ecommerce.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload and download APIs")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload file", description = "Upload a file (Admin/Seller only)")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileStorageService.storeFile(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Download file", description = "Download a file by name")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File name") @PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = "application/octet-stream";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (fileName.endsWith(".webp")) {
            contentType = "image/webp";
        } else if (fileName.endsWith(".pdf")) {
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileName:.+}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete file", description = "Delete a file (Admin only)")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "File name") @PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }
}
