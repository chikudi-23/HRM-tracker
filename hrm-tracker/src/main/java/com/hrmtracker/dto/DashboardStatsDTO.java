package com.hrmtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long totalEmployees;
    private long totalDepartments;
    private long totalHRs;

    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;

    private long totalAttendanceToday;
    private long totalAnnouncements;
}
