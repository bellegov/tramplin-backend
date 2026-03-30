package com.tramplin.backend.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String publicUrl;

    public MinioService(
            @Value("${minio.internal-url}") String internalUrl,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName,
            @Value("${minio.url}") String publicUrl) {

        this.minioClient = MinioClient.builder()
                .endpoint(internalUrl)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;
        this.publicUrl = publicUrl;
    }

    public String uploadFile(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return publicUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage());
        }
    }
}