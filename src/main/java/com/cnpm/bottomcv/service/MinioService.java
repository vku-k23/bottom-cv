package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public void createBucketIfNotExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .build()
                );
                log.info("Bucket '{}' created successfully", minioProperties.getBucketName());
            } else {
                log.info("Bucket '{}' already exists", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("Error creating bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create bucket", e);
        }
    }

    public String uploadFile(MultipartFile file, String folder) {
        try {
            createBucketIfNotExists();

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String objectName = folder + "/" + fileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded successfully: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .expiry(60 * 60 * 24) // 24 hours
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating file URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate file URL", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("File deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public List<String> listFiles(String prefix) {
        try {
            List<String> files = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .prefix(prefix)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                files.add(item.objectName());
            }

            return files;
        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list files", e);
        }
    }

    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
