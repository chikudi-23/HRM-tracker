package com.hrmtracker.controller;

import com.hrmtracker.entity.FileUploadMessage;
import com.hrmtracker.entity.User;
import com.hrmtracker.service.DashboardService;
import com.hrmtracker.service.FileUploadConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final DashboardService dashboardService;
    private final FileUploadConsumer fileUploadConsumer;

    @Value("${file.final-dir}")
    private String fileBasePath;

    // ======================== PROFILE PAGE ========================
    @GetMapping({"/profile", "/hr/profile", "/employee/profile"})
    public String profilePage(Model model,
                              Principal principal,
                              @RequestParam(value = "success", required = false) String success,
                              @RequestParam(value = "error", required = false) String error) {

        User user = dashboardService.findByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("departments", dashboardService.getAllDepartments());
        model.addAttribute("activePage", "profile");

        if (success != null) {
            model.addAttribute("successMessage", success);
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        // Decide template based on role
        if ("HR".equalsIgnoreCase(user.getRole().getName())) {
            return "profile-hr";
        } else if ("EMPLOYEE".equalsIgnoreCase(user.getRole().getName())) {
            return "profile-employee";
        } else {
            return "profile";
        }
    }

    @PostMapping({"/profile/update", "/hr/profile/update", "/employee/profile/update"})
    public String updateProfile(
            Principal principal,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "departmentId", required = false) Long departmentId
    ) {
        User user = dashboardService.findByEmail(principal.getName());
        dashboardService.updateUserProfileFields(user.getEmail(), fullName, phone, password, departmentId);
        return getRedirectWithMessage(user.getRole().getName(), "Profile updated successfully!");
    }

    // ======================== FILE UPLOAD ========================
    @PostMapping("/profile/upload-id")
    @ResponseBody
    public ResponseEntity<?> uploadIdProof(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        String jobId = UUID.randomUUID().toString();
        String tempPath = fileUploadConsumer.getTempIdProofDir() + File.separator + jobId + "_" + file.getOriginalFilename();
        file.transferTo(new File(tempPath));
        fileUploadConsumer.setProgress(jobId, 0);
        fileUploadConsumer.setStatus(jobId, "IN_PROGRESS");
        String userEmail = principal.getName();

        // ✅ Store relative path instead of /uploads/...
        FileUploadMessage msg = new FileUploadMessage(jobId, tempPath, file.getOriginalFilename(), "idProof", userEmail);
        fileUploadConsumer.send(msg);
        return ResponseEntity.ok().body(jobId);
    }

    @PostMapping("/profile/upload-resume")
    @ResponseBody
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        String jobId = UUID.randomUUID().toString();
        String tempPath = fileUploadConsumer.getTempResumeDir() + File.separator + jobId + "_" + file.getOriginalFilename();
        file.transferTo(new File(tempPath));
        fileUploadConsumer.setProgress(jobId, 0);
        fileUploadConsumer.setStatus(jobId, "IN_PROGRESS");
        String userEmail = principal.getName();

        // ✅ Store relative path
        FileUploadMessage msg = new FileUploadMessage(jobId, tempPath, file.getOriginalFilename(), "resume", userEmail);
        fileUploadConsumer.send(msg);
        return ResponseEntity.ok().body(jobId);
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String jobId = UUID.randomUUID().toString();
        String tempPath = fileUploadConsumer.getTempDir() + File.separator + jobId + "_" + file.getOriginalFilename();
        file.transferTo(new File(tempPath));
        fileUploadConsumer.setProgress(jobId, 0);
        fileUploadConsumer.setStatus(jobId, "IN_PROGRESS");

        // ✅ Store relative path
        FileUploadMessage msg = new FileUploadMessage(jobId, tempPath, file.getOriginalFilename(), "other", "");
        fileUploadConsumer.send(msg);
        return ResponseEntity.ok().body(jobId);
    }

    @GetMapping("/upload/progress/{jobId}")
    @ResponseBody
    public ResponseEntity<?> getProgress(@PathVariable String jobId) {
        Integer percent = fileUploadConsumer.getProgress(jobId);
        String status = fileUploadConsumer.getStatus(jobId);
        return ResponseEntity.ok().body(new ProgressResponse(percent == null ? 0 : percent, status == null ? "IN_PROGRESS" : status));
    }

    // ======================== FILE SERVE ========================
    @GetMapping("/uploads/{folder}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String folder, @PathVariable String filename) throws IOException {
        Path file = Paths.get(fileBasePath, folder, filename).normalize().toAbsolutePath();

        if (!file.startsWith(Paths.get(fileBasePath).toAbsolutePath())) {
            return ResponseEntity.badRequest().build();
        }

        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = getMimeTypeFromExtension(filename);
        }

        boolean inline = isViewableInBrowser(contentType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    // ======================== HELPER METHODS ========================
    private boolean isViewableInBrowser(String contentType) {
        return contentType != null &&
                (contentType.equalsIgnoreCase("application/pdf")
                        || contentType.startsWith("image/")
                        || contentType.equalsIgnoreCase("text/plain"));
    }

    private String getMimeTypeFromExtension(String filename) {
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".gif")) return "image/gif";
        if (lowerName.endsWith(".pdf")) return "application/pdf";
        if (lowerName.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    private String getRedirectWithMessage(String roleName, String msg) {
        if ("HR".equalsIgnoreCase(roleName)) {
            return "redirect:/hr/profile?success=" + msg;
        } else if ("EMPLOYEE".equalsIgnoreCase(roleName)) {
            return "redirect:/employee/profile?success=" + msg;
        } else {
            return "redirect:/profile?success=" + msg;
        }
    }

    // DTO for progress response
    public static class ProgressResponse {
        public int percent;
        public String status;
        public ProgressResponse(int percent, String status) {
            this.percent = percent;
            this.status = status;
        }
    }
}
