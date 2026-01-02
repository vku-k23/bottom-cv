package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.constant.AppConstant;
import com.cnpm.bottomcv.service.FileStorageService;
import com.cnpm.bottomcv.dto.response.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "APIs for file upload, download, and management")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload/cv")
    @Operation(summary = "Upload CV file", description = "Upload a CV file (PDF, DOC, DOCX)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadCV(
            @Parameter(description = "CV file to upload", required = true) @RequestParam("file") MultipartFile file) {

        try {
            FileUploadResponse response = fileStorageService.uploadCV(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading CV: {}", e.getMessage(), e);
            FileUploadResponse errorResponse = FileUploadResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/upload/profile-image")
    @Operation(summary = "Upload profile image", description = "Upload a profile image (JPG, JPEG, PNG, GIF, BMP)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadProfileImage(
            @Parameter(description = "Profile image to upload", required = true) @RequestParam("file") MultipartFile file) {

        try {
            FileUploadResponse response = fileStorageService.uploadProfileImage(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading profile image: {}", e.getMessage(), e);
            FileUploadResponse errorResponse = FileUploadResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/upload/company-logo")
    @Operation(summary = "Upload company logo", description = "Upload a company logo (JPG, JPEG, PNG, GIF, BMP)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logo uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('COMPANY') or hasRole('ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadCompanyLogo(
            @Parameter(description = "Company logo to upload", required = true) @RequestParam("file") MultipartFile file) {

        try {
            FileUploadResponse response = fileStorageService.uploadCompanyLogo(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading company logo: {}", e.getMessage(), e);
            FileUploadResponse errorResponse = FileUploadResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/files/download/{objectName:.+}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download file", description = "Download a file by its object name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully", content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Object name of the file to download", required = true) @PathVariable String objectName) {

        try {
            return fileStorageService.downloadFile(objectName);
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/files/url/{objectName:.+}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get file URL", description = "Get a presigned URL for a file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL generated successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getFileUrl(
            @Parameter(description = "Object name of the file", required = true) @PathVariable String objectName) {

        try {
            if (!fileStorageService.fileExists(objectName)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put(AppConstant.RESPONSE_KEY_SUCCESS, false);
                errorResponse.put(AppConstant.RESPONSE_KEY_MESSAGE, "File not found");
                return ResponseEntity.notFound().build();
            }

            String fileUrl = fileStorageService.getFileUrl(objectName);

            Map<String, Object> response = new HashMap<>();
            response.put(AppConstant.RESPONSE_KEY_SUCCESS, true);
            response.put("fileUrl", fileUrl);
            response.put("objectName", objectName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating file URL: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(AppConstant.RESPONSE_KEY_SUCCESS, false);
            errorResponse.put(AppConstant.RESPONSE_KEY_MESSAGE, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{objectName:.+}")
    @Operation(summary = "Delete file", description = "Delete a file by its object name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or @fileSecurityService.canDeleteFile(authentication.name, #objectName)")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @Parameter(description = "Object name of the file to delete", required = true) @PathVariable String objectName) {

        try {
            if (!fileStorageService.fileExists(objectName)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put(AppConstant.RESPONSE_KEY_SUCCESS, false);
                errorResponse.put(AppConstant.RESPONSE_KEY_MESSAGE, "File not found");
                return ResponseEntity.notFound().build();
            }

            fileStorageService.deleteFile(objectName);

            Map<String, Object> response = new HashMap<>();
            response.put(AppConstant.RESPONSE_KEY_SUCCESS, true);
            response.put(AppConstant.RESPONSE_KEY_MESSAGE, "File deleted successfully");
            response.put("objectName", objectName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(AppConstant.RESPONSE_KEY_SUCCESS, false);
            errorResponse.put(AppConstant.RESPONSE_KEY_MESSAGE, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list/cvs")
    @Operation(summary = "List CV files", description = "Get a list of all CV files")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listCVs() {
        try {
            List<String> files = fileStorageService.listCVs();

            Map<String, Object> response = new HashMap<>();
            response.put(AppConstant.RESPONSE_KEY_SUCCESS, true);
            response.put(AppConstant.RESPONSE_KEY_FILES, files);
            response.put(AppConstant.RESPONSE_KEY_COUNT, files.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing CV files: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(AppConstant.RESPONSE_KEY_SUCCESS, false);
            errorResponse.put(AppConstant.RESPONSE_KEY_MESSAGE, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list/profile-images")
    @Operation(summary = "List profile images", description = "Get a list of all profile images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listProfileImages() {
        try {
            List<String> files = fileStorageService.listProfileImages();

            Map<String, Object> response = new HashMap<>();
            response.put(AppConstant.RESPONSE_KEY_SUCCESS, true);
            response.put(AppConstant.RESPONSE_KEY_FILES, files);
            response.put(AppConstant.RESPONSE_KEY_COUNT, files.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing profile images: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(AppConstant.RESPONSE_KEY_SUCCESS, false);
            errorResponse.put(AppConstant.RESPONSE_KEY_MESSAGE, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list/company-logos")
    @Operation(summary = "List company logos", description = "Get a list of all company logos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listCompanyLogos() {
        try {
            List<String> files = fileStorageService.listCompanyLogos();

            Map<String, Object> response = new HashMap<>();
            response.put(AppConstant.RESPONSE_KEY_SUCCESS, true);
            response.put(AppConstant.RESPONSE_KEY_FILES, files);
            response.put(AppConstant.RESPONSE_KEY_COUNT, files.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing company logos: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(AppConstant.RESPONSE_KEY_SUCCESS, false);
            errorResponse.put(AppConstant.RESPONSE_KEY_MESSAGE, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
