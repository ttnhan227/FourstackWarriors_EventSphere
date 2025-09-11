package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.*;
import fpt.aptech.eventsphere.repositories.*;
import fpt.aptech.eventsphere.repositories.admin.*;
import fpt.aptech.eventsphere.repositories.admin.AdminEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final AdminEventRepository adminEventRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final AdminFeedbackRepository adminFeedbackRepository;
    private final AdminMediaGalleryRepository adminMediaGalleryRepository;

    public AdminDashboardDTO getDashboardData() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        // User Statistics
        dashboard.setTotalUsers(BigDecimal.valueOf(userRepository.count()));
        dashboard.setActiveUsers(BigDecimal.valueOf(userRepository.countByIsActiveTrueAndIsDeletedFalse()));
        dashboard.setSuspendedUsers(BigDecimal.valueOf(userRepository.countByIsDeletedTrue()));
        dashboard.setNewUsersThisMonth(getUsersCountThisMonth());
        dashboard.setUserGrowthRate(calculateUserGrowthRate());

        // Event Statistics
        dashboard.setTotalEvents(BigDecimal.valueOf(adminEventRepository.count()));
        dashboard.setPendingEvents(BigDecimal.valueOf(adminEventRepository.countPendingEvents()));
        dashboard.setApprovedEvents(BigDecimal.valueOf(adminEventRepository.countApprovedEvents()));
        dashboard.setRejectedEvents(BigDecimal.valueOf(adminEventRepository.countRejectedEvents()));
        dashboard.setEventsThisMonth(getEventsCountThisMonth());
        dashboard.setEventGrowthRate(calculateEventGrowthRate());

        // Department Statistics
        dashboard.setUsersByDepartment(getUsersByDepartment());
        dashboard.setEventsByDepartment(getEventsByDepartment());
        dashboard.setDepartmentDetails(getDepartmentDetails());

        // Recent Activities
        dashboard.setTodayRegistrations(getTodayRegistrations());
        dashboard.setTodayEventCreations(getTodayEventCreations());
        dashboard.setPendingFeedbackReviews(getPendingFeedbackReviews());
//        dashboard.setPendingMediaReviews(getPendingMediaReviews());

        // Performance Metrics
        dashboard.setAverageEventRating(getAverageEventRating());
        dashboard.setTotalRegistrations(getTotalRegistrations());
        dashboard.setCompletedEvents(getCompletedEvents());
        dashboard.setCertificatesIssued(getCertificatesIssued());

        // System Alerts
        dashboard.setSystemAlerts(getSystemAlerts());
        dashboard.setCriticalAlerts(getCriticalAlerts());

        // Charts Data
        dashboard.setUserRegistrationChart(getUserRegistrationChartData());
        dashboard.setEventCreationChart(getEventCreationChartData());
        dashboard.setDepartmentDistributionChart(getDepartmentDistributionChartData());

        dashboard.setLastUpdated(LocalDateTime.now());

        return dashboard;
    }

    private BigDecimal getUsersCountThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return BigDecimal.valueOf(userRepository.countByCreatedAtAfter(startOfMonth));
    }

    private BigDecimal getEventsCountThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return BigDecimal.valueOf(adminEventRepository.countByCreatedAtAfter(startOfMonth));
    }

    private BigDecimal calculateUserGrowthRate() {
        LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonth = thisMonth.minusMonths(1);

        long thisMonthCount = userRepository.countByCreatedAtBetween(thisMonth, LocalDateTime.now());
        long lastMonthCount = userRepository.countByCreatedAtBetween(lastMonth, thisMonth);

        if (lastMonthCount == 0) return BigDecimal.valueOf(100);

        double growthRate = ((double)(thisMonthCount - lastMonthCount) / lastMonthCount) * 100;
        return BigDecimal.valueOf(Math.round(growthRate * 100.0) / 100.0);
    }

    private BigDecimal calculateEventGrowthRate() {
        LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonth = thisMonth.minusMonths(1);

        long thisMonthCount = adminEventRepository.countByCreatedAtAfter(thisMonth);
        long lastMonthCount = adminEventRepository.countByCreatedAtAfter(lastMonth) - thisMonthCount;

        if (lastMonthCount == 0) return BigDecimal.valueOf(100);

        double growthRate = ((double)(thisMonthCount - lastMonthCount) / lastMonthCount) * 100;
        return BigDecimal.valueOf(Math.round(growthRate * 100.0) / 100.0);
    }

    private Map<String, Long> getUsersByDepartment() {
        List<Object[]> results = userRepository.countUsersByDepartment();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    private Map<String, Long> getEventsByDepartment() {
        List<Object[]> results = adminEventRepository.countEventsByDepartment();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    private List<DepartmentStatsDTO> getDepartmentDetails() {
        Map<String, Long> usersByDept = getUsersByDepartment();
        Map<String, Long> eventsByDept = getEventsByDepartment();

        return usersByDept.keySet().stream()
                .map(dept -> {
                    DepartmentStatsDTO dto = new DepartmentStatsDTO();
                    dto.setDepartmentName(dept);
                    dto.setTotalUsers(usersByDept.getOrDefault(dept, 0L));
                    dto.setTotalEvents(eventsByDept.getOrDefault(dept, 0L));
                    // Add more calculations as needed
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal getTodayRegistrations() {
        return BigDecimal.valueOf(0); // Placeholder
    }

    private BigDecimal getTodayEventCreations() {
        LocalDate today = LocalDate.now();
        return BigDecimal.valueOf(adminEventRepository.countCreatedToday(today));
    }

    private BigDecimal getPendingFeedbackReviews() {
        return adminFeedbackRepository.countPendingReviews();
    }

//    private BigDecimal getPendingMediaReviews() {
//        return mediaGalleryRepository.countPendingReviews();
//    }

    private BigDecimal getAverageEventRating() {
        Double avg = adminEventRepository.getAverageEventRating();
        return avg != null ? BigDecimal.valueOf(avg) : BigDecimal.ZERO;
    }

    private BigDecimal getTotalRegistrations() {
        return BigDecimal.valueOf(0);
    }

    private BigDecimal getCompletedEvents() {
        return BigDecimal.valueOf(adminEventRepository.countCompletedEvents());
    }

    private BigDecimal getCertificatesIssued() {
        return BigDecimal.valueOf(0);
    }

    private List<SystemAlertDTO> getSystemAlerts() {
        List<SystemAlertDTO> alerts = new ArrayList<>();

        // Check for system issues and create alerts
        long inactiveUsers = userRepository.count() - userRepository.countByIsActiveTrueAndIsDeletedFalse();
        if (inactiveUsers > 10) {
            alerts.add(new SystemAlertDTO(
                    "inactive_users",
                    "WARNING",
                    "Nhiều tài khoản chưa kích hoạt",
                    inactiveUsers + " tài khoản chưa được kích hoạt",
                    "fas fa-exclamation-triangle",
                    "warning",
                    LocalDateTime.now(),
                    false,
                    "/admin/users?filter=inactive"
            ));
        }

        long pendingEvents = adminEventRepository.countPendingEvents();
        if (pendingEvents > 5) {
            alerts.add(new SystemAlertDTO(
                    "pending_events",
                    "INFO",
                    "Sự kiện chờ duyệt",
                    pendingEvents + " sự kiện đang chờ phê duyệt",
                    "fas fa-clock",
                    "info",
                    LocalDateTime.now(),
                    false,
                    "/admin/events?status=pending"
            ));
        }

        return alerts;
    }

    private BigDecimal getCriticalAlerts() {
        return BigDecimal.valueOf(getSystemAlerts().stream()
                .mapToLong(alert -> "ERROR".equals(alert.getType()) || "CRITICAL".equals(alert.getType()) ? 1 : 0)
                .sum());
    }

    private List<ChartDataDTO> getUserRegistrationChartData() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        List<Object[]> results = userRepository.getUserRegistrationStats(startDate);

        return results.stream()
                .map(result -> new ChartDataDTO(
                        result[0].toString(),
                        BigDecimal.valueOf((Long) result[1]),
                        "#007bff",
                        result[0].toString()
                ))
                .collect(Collectors.toList());
    }

    private List<ChartDataDTO> getEventCreationChartData() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        List<Object[]> results = adminEventRepository.getEventCreationStats(startDate);

        return results.stream()
                .map(result -> new ChartDataDTO(
                        result[0].toString(),
                        BigDecimal.valueOf((Long) result[1]),
                        "#28a745",
                        result[0].toString()
                ))
                .collect(Collectors.toList());
    }

    private List<ChartDataDTO> getDepartmentDistributionChartData() {
        Map<String, Long> usersByDept = getUsersByDepartment();
        List<String> colors = Arrays.asList("#007bff", "#28a745", "#ffc107", "#dc3545", "#17a2b8");

        return usersByDept.entrySet().stream()
                .map(entry -> new ChartDataDTO(
                        entry.getKey(),
                        BigDecimal.valueOf(entry.getValue()),
                        colors.get(Math.abs(entry.getKey().hashCode()) % colors.size()),
                        ""
                ))
                .collect(Collectors.toList());
    }
}