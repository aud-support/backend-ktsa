package com.ktsa.foosball.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client; // injected from S3Config bean
    private final ObjectMapper objectMapper;  // Add this bean or use new ObjectMapper()




    @Value("${aws.region}")
    private String region;

    public String uploadFile(MultipartFile file, String bucket, String folder) {
        String key = folder + "/" +  UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload banner to S3", e);
        }

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    // Upload/Overwrite JSON content to a fixed S3 key
    public void uploadJson(Object data, String bucket, String key) {
        try {
            String json = objectMapper.writeValueAsString(data);
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)                        // e.g. "homepage/content.json"
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(json, StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload JSON to S3", e);
        }
    }

    // Read JSON from S3 and return as a Map (or any class)
    public Map<String, Object> readJson(String bucket, String key) {
        try {
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)                        // e.g. "homepage/content.json"
                            .build()
            );
            return objectMapper.readValue(s3Object, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from S3", e);
        }
    }

}