package com.hrmtracker.controller;

import com.hrmtracker.dto.AuditLogDTO;
import com.hrmtracker.dto.EmployeeDTO;
import com.hrmtracker.dto.HrDto;
import com.hrmtracker.entity.*;
import com.hrmtracker.repository.*;
import com.hrmtracker.service.AuditLogService;
import com.hrmtracker.service.DashboardService;
import com.hrmtracker.service.impl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final DepartmentRepository departmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AuditLogService auditLogService;
    private final AttendanceRepository attendanceRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return "redirect:/login?error";
        }

        model.addAttribute("user", user);
        String roleName = user.getRole().getName();

        switch (roleName) {
            case "ADMIN":
                model.addAttribute("dashboardStats", dashboardService.getAdminDashboardStats());
                return "dashboard-admin";
            case "HR":
                model.addAttribute("dashboardStats", dashboardService.getHRDashboardStats());
                return "dashboard-hr";
            case "EMPLOYEE":
                model.addAttribute("dashboardStats", dashboardService.getEmployeeDashboardStats(email));
                return "dashboard-employee";
            default:
                return "redirect:/login?error";
        }
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !"ADMIN".equals(user.getRole().getName())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("user", user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<AuditLogDTO> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .map(log -> new AuditLogDTO(
                        log.getTimestamp().format(formatter), // convert LocalDateTime -> String
                        log.getUsername(),
                        log.getEntity(),
                        log.getAction()
                ))
                .collect(Collectors.toList());

        model.addAttribute("logs", logs);
        return "audit-logs";
    }

    // ---------- Employees ----------
    @ResponseBody
    @GetMapping("/employees")
    public List<EmployeeDTO> getEmployees() {
        return dashboardService.getAllEmployees();
    }

    // ---------- HRs ----------
    @ResponseBody
    @GetMapping("/hrs")
    public List<HrDto> getAllHRs() {
        return dashboardService.getAllHRs();
    }

    // ---------- Departments ----------
    @ResponseBody
    @GetMapping("/api/departments")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @ResponseBody
    @PostMapping("/api/departments")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Department createDepartment(@RequestBody Department department) {
        Department savedDept = departmentRepository.save(department);
        auditLogService.logAction("CREATE", "Department", null, savedDept.toString());
        return savedDept;
    }

    @ResponseBody
    @PutMapping("/api/departments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Department updateDepartment(@PathVariable Long id, @RequestBody Department updatedDept) {
        Department oldDept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        String oldValue = oldDept.toString();

        oldDept.setName(updatedDept.getName());
        oldDept.setDescription(updatedDept.getDescription());

        Department savedDept = departmentRepository.save(oldDept);

        auditLogService.logAction("UPDATE", "Department", oldValue, savedDept.toString());

        return savedDept;
    }

    // ================== Announcements CRUD ==================

    // List (public view / employee view allowed)
    @ResponseBody
    @GetMapping("/announcements")
    public List<Map<String, Object>> getAllAnnouncements() {
        return announcementRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("title", a.getTitle());
                    map.put("content", a.getContent());
                    map.put("createdAt", a.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Create (ADMIN/HR only)
    @ResponseBody
    @PostMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createAnnouncement(@RequestBody Map<String, String> payload) {
        String title = Optional.ofNullable(payload.get("title")).orElse("").trim();
        String content = Optional.ofNullable(payload.get("content")).orElse("").trim();

        if (title.isEmpty() || content.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and Content are required"));
        }

        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        // Agar entity me @PrePersist se createdAt set hota hai to yeh optional hai:
        if (a.getCreatedAt() == null) {
            a.setCreatedAt(LocalDateTime.now());
        }

        Announcement saved = announcementRepository.save(a);
        auditLogService.logAction("CREATE", "Announcement", null, saved.toString());

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Announcement created successfully");
        resp.put("id", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // Update (ADMIN/HR only)
    @ResponseBody
    @PutMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> updateAnnouncement(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<Announcement> opt = announcementRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Announcement not found"));
        }
        Announcement announcement = opt.get();

        String oldValue = announcement.toString();

        String title = Optional.ofNullable(payload.get("title")).orElse("").trim();
        String content = Optional.ofNullable(payload.get("content")).orElse("").trim();
        if (title.isEmpty() || content.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and Content are required"));
        }

        announcement.setTitle(title);
        announcement.setContent(content);
        Announcement saved = announcementRepository.save(announcement);

        auditLogService.logAction("UPDATE", "Announcement", oldValue, saved.toString());
        return ResponseEntity.ok(Map.of("message", "Announcement updated successfully"));
    }

    // Delete (ADMIN/HR only)
    @ResponseBody
    @DeleteMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        Optional<Announcement> opt = announcementRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Announcement not found"));
        }
        Announcement a = opt.get();
        announcementRepository.deleteById(id);

        auditLogService.logAction("DELETE", "Announcement", a.toString(), null);
        return ResponseEntity.ok(Map.of("message", "Announcement deleted successfully"));
    }
// ================== Attendance Endpoints ==================

    @PostMapping("/attendance/checkin")
    @ResponseBody
    public Attendance checkIn() {
        String email = getCurrentUserEmail();
        return ((DashboardServiceImpl) dashboardService).checkIn(email);
    }

    @PostMapping("/attendance/checkout")
    @ResponseBody
    public Attendance checkOut() {
        String email = getCurrentUserEmail();
        return ((DashboardServiceImpl) dashboardService).checkOut(email);
    }

    @GetMapping("/attendance/calendar")
    @ResponseBody
    public List<Map<String, Object>> getMyAttendance() {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Attendance> attendanceList = attendanceRepository.findByUser(user);

        return attendanceList.stream().map(att -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", att.getDate().toString());

            String status;
            if (Boolean.TRUE.equals(att.getLeave1())) {
                status = "Leave";
            } else if (att.getCheckInTime() != null) {
                status = "Present";
            } else {
                status = "Absent";
            }

            map.put("status", status);
            map.put("description", "Check-in: " +
                    (att.getCheckInTime() != null ? att.getCheckInTime().toString() : "N/A") +
                    ", Check-out: " +
                    (att.getCheckOutTime() != null ? att.getCheckOutTime().toString() : "N/A"));

            return map;
        }).collect(Collectors.toList());
    }


    @GetMapping("/api/attendance/day/{date}")
    @ResponseBody
    public Map<String, Object> getDayAttendanceJson(@PathVariable String date) {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate localDate = LocalDate.parse(date);
        Attendance attendance = attendanceRepository.findByUserAndDate(user, localDate);

        Map<String, Object> result = new HashMap<>();
        result.put("date", localDate.toString());

        if (attendance != null) {
            result.put("checkInTime", timeOrNA(attendance.getCheckInTime()));
            result.put("checkOutTime", timeOrNA(attendance.getCheckOutTime()));
            result.put("hoursWorked", calculateHours(attendance.getCheckInTime(), attendance.getCheckOutTime()));

            String status;
            if (Boolean.TRUE.equals(attendance.getLeave1())) {
                status = "Leave";
            } else if (attendance.getCheckInTime() != null) {
                status = "Present";
            } else {
                status = "Absent";
            }

            result.put("status", status);
        } else {
            result.put("status", "Absent");
            result.put("checkInTime", "N/A");
            result.put("checkOutTime", "N/A");
            result.put("hoursWorked", 0);
        }

        return result;
    }

// ================== Utility Methods ==================

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private String timeOrNA(LocalTime time) {
        return time != null ? time.toString() : "N/A";
    }

    private double calculateHours(LocalTime checkIn, LocalTime checkOut) {
        if (checkIn != null && checkOut != null) {
            long minutes = ChronoUnit.MINUTES.between(checkIn, checkOut);
            return minutes / 60.0; // decimal hours
        }
        return 0;
    }

    // ================== Users for Attendance Card ==================
    @ResponseBody
    @GetMapping("/api/users")
    public List<Map<String, Object>> getUsers(@RequestParam(required = false) String excludeRole) {
        List<User> users = userRepository.findAll();
        if (excludeRole != null) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && !excludeRole.equalsIgnoreCase(u.getRole().getName()))
                    .collect(Collectors.toList());
        }
        return users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getFullName());
            map.put("email", u.getEmail());
            map.put("department", u.getDepartment() != null ? u.getDepartment().getName() : null);
            map.put("role", u.getRole() != null ? u.getRole().getName() : null);
            return map;
        }).collect(Collectors.toList());
    }

    // Get attendance for a specific user (all days)
    @ResponseBody
    @GetMapping("/api/attendance/{userId}")
    public List<Map<String, Object>> getAttendanceByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Attendance> attendanceList = attendanceRepository.findByUser(user);

        return attendanceList.stream().map(att -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", att.getDate().toString());

            String status;
            if (Boolean.TRUE.equals(att.getLeave1())) {
                status = "Leave";
            } else if (att.getCheckInTime() != null) {
                status = "Present";
            } else {
                status = "Absent";
            }

            map.put("status", status);
            map.put("checkInTime", timeOrNA(att.getCheckInTime()));
            map.put("checkOutTime", timeOrNA(att.getCheckOutTime()));
            map.put("hoursWorked", calculateHours(att.getCheckInTime(), att.getCheckOutTime()));

            return map;
        }).collect(Collectors.toList());
    }

    // Get attendance for a specific user on a specific day
    @ResponseBody
    @GetMapping("/api/attendance/{userId}/day/{date}")
    public Map<String, Object> getAttendanceByUserAndDay(@PathVariable Long userId,
                                                         @PathVariable String date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate localDate = LocalDate.parse(date);
        Attendance attendance = attendanceRepository.findByUserAndDate(user, localDate);

        Map<String, Object> result = new HashMap<>();
        result.put("date", localDate.toString());

        if (attendance != null) {
            result.put("checkInTime", timeOrNA(attendance.getCheckInTime()));
            result.put("checkOutTime", timeOrNA(attendance.getCheckOutTime()));
            result.put("hoursWorked", calculateHours(attendance.getCheckInTime(), attendance.getCheckOutTime()));

            String status;
            if (Boolean.TRUE.equals(attendance.getLeave1())) {
                status = "Leave";
            } else if (attendance.getCheckInTime() != null) {
                status = "Present";
            } else {
                status = "Absent";
            }

            result.put("status", status);
        } else {
            result.put("status", "Absent");
            result.put("checkInTime", "N/A");
            result.put("checkOutTime", "N/A");
            result.put("hoursWorked", 0);
        }

        return result;
    }


}