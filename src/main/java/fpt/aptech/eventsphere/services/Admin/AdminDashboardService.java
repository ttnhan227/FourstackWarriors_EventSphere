package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.*;
import fpt.aptech.eventsphere.repositories.*;
import fpt.aptech.eventsphere.repositories.admin.*;
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
    private final CertificateRepository certificateRepository;

    public AdminDashboardDTO getDashboardData() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        try {
            // User Statistics với error handling
            long totalUsersCount = userRepository.count();
            long activeUsersCount = 0;
            long suspendedUsersCount = 0;

            try {
                activeUsersCount = userRepository.countByIsActiveTrueAndIsDeletedFalse();
            } catch (Exception e) {
                System.err.println("Error getting active users count: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                //nguoi dung không hoạt động
                suspendedUsersCount = totalUsersCount - activeUsersCount;
            } catch (Exception e) {
                System.err.println("Error getting suspended users count: " + e.getMessage());
                e.printStackTrace();
            }

            // Set values với null check
            dashboard.setTotalUsers(BigDecimal.valueOf(totalUsersCount));
            dashboard.setActiveUsers(BigDecimal.valueOf(activeUsersCount));
            dashboard.setSuspendedUsers(BigDecimal.valueOf(suspendedUsersCount));

            // Debug after setting
            System.out.println("Dashboard totalUsers: " + dashboard.getTotalUsers());
            System.out.println("Dashboard activeUsers: " + dashboard.getActiveUsers());
            System.out.println("Dashboard suspendedUsers: " + dashboard.getSuspendedUsers());

            // Set other user stats
            dashboard.setNewUsersThisMonth(getUsersCountThisMonth());
            dashboard.setUserGrowthRate(calculateUserGrowthRate());

        } catch (Exception e) {
            System.err.println("Error in getDashboardData: " + e.getMessage());
            e.printStackTrace();

            // Fallback values
            dashboard.setTotalUsers(BigDecimal.ZERO);
            dashboard.setActiveUsers(BigDecimal.ZERO);
            dashboard.setSuspendedUsers(BigDecimal.ZERO);
            dashboard.setNewUsersThisMonth(BigDecimal.ZERO);
            dashboard.setUserGrowthRate(BigDecimal.ZERO);
        }

        // Event Statistics
        try {
            dashboard.setTotalEvents(BigDecimal.valueOf(adminEventRepository.count()));
            dashboard.setPendingEvents(BigDecimal.valueOf(adminEventRepository.countPendingEvents()));
            dashboard.setApprovedEvents(BigDecimal.valueOf(adminEventRepository.countApprovedEvents()));
            dashboard.setRejectedEvents(BigDecimal.valueOf(adminEventRepository.countRejectedEvents()));
            dashboard.setEventsThisMonth(getEventsCountThisMonth());
            dashboard.setEventGrowthRate(calculateEventGrowthRate());
        } catch (Exception e) {
            System.err.println("Error getting event statistics: " + e.getMessage());
            dashboard.setTotalEvents(BigDecimal.ZERO);
            dashboard.setPendingEvents(BigDecimal.ZERO);
            dashboard.setApprovedEvents(BigDecimal.ZERO);
            dashboard.setRejectedEvents(BigDecimal.ZERO);
            dashboard.setEventsThisMonth(BigDecimal.ZERO);
            dashboard.setEventGrowthRate(BigDecimal.ZERO);
        }

        // Department Statistics
        try {
            dashboard.setUsersByDepartment(getUsersByDepartment());
            dashboard.setEventsByDepartment(getEventsByDepartment());
            dashboard.setDepartmentDetails(getDepartmentDetails());
        } catch (Exception e) {
            System.err.println("Error getting department statistics: " + e.getMessage());
            dashboard.setUsersByDepartment(new HashMap<>());
            dashboard.setEventsByDepartment(new HashMap<>());
            dashboard.setDepartmentDetails(new ArrayList<>());
        }

        // Recent Activities
        try {
            dashboard.setTodayRegistrations(getTodayRegistrations());
            dashboard.setTodayEventCreations(getTodayEventCreations());
            dashboard.setPendingFeedbackReviews(getPendingFeedbackReviews());
        } catch (Exception e) {
            System.err.println("Error getting recent activities: " + e.getMessage());
            dashboard.setTodayRegistrations(BigDecimal.ZERO);
            dashboard.setTodayEventCreations(BigDecimal.ZERO);
            dashboard.setPendingFeedbackReviews(BigDecimal.ZERO);
        }

        // Performance Metrics
        try {
            dashboard.setAverageEventRating(getAverageEventRating());
            dashboard.setTotalRegistrations(getTotalRegistrations());
            dashboard.setCompletedEvents(getCompletedEvents());
            dashboard.setCertificatesIssued(getCertificatesIssued());
        } catch (Exception e) {
            System.err.println("Error getting performance metrics: " + e.getMessage());
            dashboard.setAverageEventRating(BigDecimal.ZERO);
            dashboard.setTotalRegistrations(BigDecimal.ZERO);
            dashboard.setCompletedEvents(BigDecimal.ZERO);
            dashboard.setCertificatesIssued(BigDecimal.ZERO);
        }

        // System Alerts
        try {
            dashboard.setSystemAlerts(getSystemAlerts());
            dashboard.setCriticalAlerts(getCriticalAlerts());
        } catch (Exception e) {
            System.err.println("Error getting system alerts: " + e.getMessage());
            dashboard.setSystemAlerts(new ArrayList<>());
            dashboard.setCriticalAlerts(BigDecimal.ZERO);
        }

        // Charts Data
        try {
            dashboard.setUserRegistrationChart(getUserRegistrationChartData());
            dashboard.setEventCreationChart(getEventCreationChartData());
            dashboard.setDepartmentDistributionChart(getDepartmentDistributionChartData());
        } catch (Exception e) {
            System.err.println("Error getting charts data: " + e.getMessage());
            dashboard.setUserRegistrationChart(new ArrayList<>());
            dashboard.setEventCreationChart(new ArrayList<>());
            dashboard.setDepartmentDistributionChart(new ArrayList<>());
        }

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
        try {
            LocalDate today = LocalDate.now();
            long count = userRepository.countByCreatedAtToday(today);
            return BigDecimal.valueOf(count);
        } catch (Exception e) {
            System.err.println("Error getting today registrations: " + e.getMessage());
            return BigDecimal.ZERO;
        }

    }

    private BigDecimal getTodayEventCreations() {
        try {
            LocalDate today = LocalDate.now();
            long count = adminEventRepository.countCreatedToday(today);
            return BigDecimal.valueOf(count);
        } catch (Exception e) {
            System.err.println("Error getting today event creations: " + e.getMessage());
            return BigDecimal.ZERO;
        }

    }

    private BigDecimal getPendingFeedbackReviews() {
        try {
            BigDecimal count = adminFeedbackRepository.countPendingReviews();
            return count != null ? count : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error getting pending feedback reviews: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

//    private BigDecimal getPendingMediaReviews() {
//        return mediaGalleryRepository.countPendingReviews();
//    }

    private BigDecimal getAverageEventRating() {
        Double avg = adminEventRepository.getAverageEventRating();
        return avg != null ? BigDecimal.valueOf(avg) : BigDecimal.ZERO;
    }

    private BigDecimal getTotalRegistrations() {
        try {
            long count = userRepository.count();
            return BigDecimal.valueOf(count);
        } catch (Exception e) {
            System.err.println("Error getting total registrations: " + e.getMessage());
            return BigDecimal.ZERO;
        }

    }

    private BigDecimal getCompletedEvents() {
        return BigDecimal.valueOf(adminEventRepository.countCompletedEvents());
    }

    private BigDecimal getCertificatesIssued() {
        try {
            long count = certificateRepository.count();
            System.out.println("Total certificates issued from DB: " + count);
            return BigDecimal.valueOf(count);
        } catch (Exception e) {
            System.err.println("Error getting certificates issued: " + e.getMessage());
            return BigDecimal.ZERO;
        }

    }

    private List<SystemAlertDTO> getSystemAlerts() {
        List<SystemAlertDTO> alerts = new ArrayList<>();

        // Check for system issues and create alerts
        long inactiveUsers = userRepository.count() - userRepository.countByIsActiveTrueAndIsDeletedFalse();
        if (inactiveUsers > 10) {
            alerts.add(new SystemAlertDTO(
                    "inactive_users",
                    "WARNING",
                    "Many unactivated accounts",
                    inactiveUsers + " accounts are not activated",
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
                    "Pending events",
                    pendingEvents + " events are pending approval",
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
                .map(result -> {
                    ChartDataDTO dto = new ChartDataDTO(
                            result[0].toString(),
                            BigDecimal.valueOf((Long) result[1])
                    );
                    dto.setColor("#007bff");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<ChartDataDTO> getEventCreationChartData() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        List<Object[]> results = adminEventRepository.getEventCreationStats(startDate);

        return results.stream()
                .map(result -> {
                    ChartDataDTO dto = new ChartDataDTO(
                            result[0].toString(),
                            BigDecimal.valueOf((Long) result[1])
                    );
                    dto.setColor("#28a745");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<ChartDataDTO> getDepartmentDistributionChartData() {
        Map<String, Long> usersByDept = getUsersByDepartment();
        List<String> colors = Arrays.asList("#007bff", "#28a745", "#ffc107", "#dc3545", "#17a2b8");

        return usersByDept.entrySet().stream()
                .map(entry -> {
                    ChartDataDTO dto = new ChartDataDTO(
                            entry.getKey(),
                            BigDecimal.valueOf(entry.getValue())
                    );
                    dto.setColor(colors.get(Math.abs(entry.getKey().hashCode()) % colors.size()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

}