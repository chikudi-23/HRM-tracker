package com.hrmtracker.service;

import com.hrmtracker.config.RabbitMQConfig;
import com.hrmtracker.entity.FileUploadMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final DashboardService dashboardService;

    // Upload directories
    private final String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads";
    private final String tempDir = uploadsDir + File.separator + "temp";
    private final String finalDir = uploadsDir + File.separator + "final";
    private final String tempIdProofDir = tempDir + File.separator + "id_proofs";
    private final String tempResumeDir = tempDir + File.separator + "resumes";
    private final String finalIdProofDir = finalDir + File.separator + "id_proofs";
    private final String finalResumeDir = finalDir + File.separator + "resumes";

    private final ConcurrentHashMap<String, Integer> progressMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> statusMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        createDirectoryIfNotExists(uploadsDir);
        createDirectoryIfNotExists(tempDir);
        createDirectoryIfNotExists(finalDir);
        createDirectoryIfNotExists(tempIdProofDir);
        createDirectoryIfNotExists(tempResumeDir);
        createDirectoryIfNotExists(finalIdProofDir);
        createDirectoryIfNotExists(finalResumeDir);
        log.info("Upload directories initialized under {}", uploadsDir);
    }

    private void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("Created directory: {}", path);
        }
    }

    public void setProgress(String jobId, int percent) {
        progressMap.put(jobId, percent);
    }

    public Integer getProgress(String jobId) {
        return progressMap.get(jobId);
    }

    public void setStatus(String jobId, String status) {
        statusMap.put(jobId, status);
    }

    public String getStatus(String jobId) {
        return statusMap.get(jobId);
    }

    /** Send message to RabbitMQ */
    public void send(FileUploadMessage msg) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FILE_UPLOAD_EXCHANGE,
                RabbitMQConfig.FILE_UPLOAD_ROUTING_KEY,
                msg
        );
        log.info("Message sent to queue for jobId={}, file={}", msg.getJobId(), msg.getOriginalFilename());
    }

    /** Receive and process file from RabbitMQ */
    @RabbitListener(queues = RabbitMQConfig.FILE_UPLOAD_QUEUE)
    public void receive(FileUploadMessage message) {
        String jobId = message.getJobId();
        File tempFile = new File(message.getTempFilePath());

        if (!tempFile.exists()) {
            log.error("Temp file not found for jobId={}, path={}", jobId, tempFile.getAbsolutePath());
            setStatus(jobId, "FAILED");
            return;
        }

        String targetDir;
        String typeFolder;
        if ("idProof".equalsIgnoreCase(message.getFileType())) {
            targetDir = getFinalIdProofDir();
            typeFolder = "id_proofs";
        } else if ("resume".equalsIgnoreCase(message.getFileType())) {
            targetDir = getFinalResumeDir();
            typeFolder = "resumes";
        } else {
            targetDir = getFinalDir();
            typeFolder = "";
        }

        File finalFile = new File(targetDir, tempFile.getName());

        try {
            // ✅ Move file atomically from temp → final
            Files.move(tempFile.toPath(), finalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            setProgress(jobId, 100);
            setStatus(jobId, "COMPLETED");

            // ✅ FIXED: store only relative path in DB (no `/uploads/` prefix)
            String relativePath = (typeFolder.isEmpty() ? "" : typeFolder + "/") + finalFile.getName();
            String userEmail = message.getUserEmail();

            // Update DB with relative path
            if ("idProof".equalsIgnoreCase(message.getFileType())) {
                dashboardService.updateIdProof(userEmail, relativePath);
            } else if ("resume".equalsIgnoreCase(message.getFileType())) {
                dashboardService.updateResume(userEmail, relativePath);
            }

            log.info("File processed successfully: jobId={}, savedTo={}, relativePath={}",
                    jobId, finalFile.getAbsolutePath(), relativePath);

        } catch (IOException e) {
            setStatus(jobId, "FAILED");
            log.error("File processing failed for jobId=" + jobId, e);
        } finally {
            // Cleanup to avoid memory leak
            progressMap.remove(jobId);
            statusMap.remove(jobId);
        }
    }

    public String getFinalDir() { return finalDir; }
    public String getTempDir() { return tempDir; }
    public String getTempIdProofDir() { return tempIdProofDir; }
    public String getTempResumeDir() { return tempResumeDir; }
    public String getFinalIdProofDir() { return finalIdProofDir; }
    public String getFinalResumeDir() { return finalResumeDir; }
}
