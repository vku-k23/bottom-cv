package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.response.FileUploadResponse;
import com.cnpm.bottomcv.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final MinioService minioService;

    private static final List<String> ALLOWED_CV_EXTENSIONS = Arrays.asList(
            ".pdf", ".doc", ".docx"
    );

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp"
    );

    public FileUploadResponse uploadCV(MultipartFile file) {
        validateCVFile(file);
        String objectName = minioService.uploadFile(file, "cvs");
        String fileUrl = minioService.getFileUrl(objectName);

        return FileUploadResponse.builder()
                .success(true)
                .message("CV uploaded successfully")
                .objectName(objectName)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    public FileUploadResponse uploadProfileImage(MultipartFile file) {
        validateImageFile(file);
        String objectName = minioService.uploadFile(file, "profile-images");
        String fileUrl = minioService.getFileUrl(objectName);

        return FileUploadResponse.builder()
                .success(true)
                .message("Profile image uploaded successfully")
                .objectName(objectName)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    public FileUploadResponse uploadCompanyLogo(MultipartFile file) {
        validateImageFile(file);
        String objectName = minioService.uploadFile(file, "company-logos");
        String fileUrl = minioService.getFileUrl(objectName);

        return FileUploadResponse.builder()
                .success(true)
                .message("Company logo uploaded successfully")
                .objectName(objectName)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    public ResponseEntity<Resource> downloadFile(String objectName) {
        try {
            InputStream inputStream = minioService.downloadFile(objectName);
            Resource resource = new InputStreamResource(inputStream);

            String filename = objectName.substring(objectName.lastIndexOf("/") + 1);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public String getFileUrl(String objectName) {
        return minioService.getFileUrl(objectName);
    }

    public void deleteFile(String objectName) {
        minioService.deleteFile(objectName);
    }

    public List<String> listCVs() {
        return minioService.listFiles("cvs/");
    }

    public List<String> listProfileImages() {
        return minioService.listFiles("profile-images/");
    }

    public List<String> listCompanyLogos() {
        return minioService.listFiles("company-logos/");
    }

    public boolean fileExists(String objectName) {
        return minioService.fileExists(objectName);
    }

    private void validateCVFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileStorageException("File name cannot be null");
        }

        String extension = originalFilename.toLowerCase().substring(originalFilename.lastIndexOf("."));
        if (!ALLOWED_CV_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Only PDF, DOC, and DOCX files are allowed for CVs");
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileStorageException("File size cannot exceed 10MB");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileStorageException("File name cannot be null");
        }

        String extension = originalFilename.toLowerCase().substring(originalFilename.lastIndexOf("."));
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Only JPG, JPEG, PNG, GIF, and BMP files are allowed for images");
        }

        // Check file size (max 5MB for images)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileStorageException("Image size cannot exceed 5MB");
        }
    }
}
