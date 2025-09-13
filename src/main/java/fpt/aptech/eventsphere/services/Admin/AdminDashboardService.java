package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.AdminDashboardDTO;
import fpt.aptech.eventsphere.dto.admin.ChartDataDTO;
import fpt.aptech.eventsphere.dto.admin.DepartmentStatsDTO;
import fpt.aptech.eventsphere.dto.admin.SystemAlertDTO;
import fpt.aptech.eventsphere.repositories.UserDetailsRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminCertificateRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminFeedbackRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminMediaGalleryRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminUserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final AdminFeedbackRepository adminFeedbackRepository;
    private final AdminMediaGalleryRepository adminMediaGalleryRepository;
    private final AdminCertificateRepository adminCertificateRepository;

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


        // Department Statistics
        try {
            dashboard.setUsersByDepartment(getUsersByDepartment());
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
            dashboard.setPendingFeedbackReviews(getPendingFeedbackReviews());
        } catch (Exception e) {
            System.err.println("Error getting recent activities: " + e.getMessage());
            dashboard.setTodayRegistrations(BigDecimal.ZERO);
            dashboard.setTodayEventCreations(BigDecimal.ZERO);
            dashboard.setPendingFeedbackReviews(BigDecimal.ZERO);
        }

        // Performance Metrics
        try {
            dashboard.setTotalRegistrations(getTotalRegistrations());
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


    private BigDecimal calculateUserGrowthRate() {
        LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonth = thisMonth.minusMonths(1);

        long thisMonthCount = userRepository.countByCreatedAtBetween(thisMonth, LocalDateTime.now());
        long lastMonthCount = userRepository.countByCreatedAtBetween(lastMonth, thisMonth);

        if (lastMonthCount == 0) return BigDecimal.valueOf(100);

        double growthRate = ((double) (thisMonthCount - lastMonthCount) / lastMonthCount) * 100;
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


    private List<DepartmentStatsDTO> getDepartmentDetails() {
        Map<String, Long> usersByDept = getUsersByDepartment();

        return usersByDept.keySet().stream()
                .map(dept -> {
                    DepartmentStatsDTO dto = new DepartmentStatsDTO();
                    dto.setDepartmentName(dept);
                    dto.setTotalUsers(usersByDept.getOrDefault(dept, 0L));
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


    private BigDecimal getTotalRegistrations() {
        try {
            long count = userRepository.count();
            return BigDecimal.valueOf(count);
        } catch (Exception e) {
            System.err.println("Error getting total registrations: " + e.getMessage());
            return BigDecimal.ZERO;
        }

    }


    private BigDecimal getCertificatesIssued() {
        try {
            long count = adminCertificateRepository.count();
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