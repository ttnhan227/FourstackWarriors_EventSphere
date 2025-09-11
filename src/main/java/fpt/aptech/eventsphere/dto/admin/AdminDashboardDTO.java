package fpt.aptech.eventsphere.dto.admin;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    
    // User Statistics
    private BigDecimal totalUsers;
    private BigDecimal activeUsers;
    private BigDecimal suspendedUsers;
    private BigDecimal newUsersThisMonth;
    private BigDecimal userGrowthRate;
    
    // Event Statistics
    private BigDecimal totalEvents;
    private BigDecimal pendingEvents;
    private BigDecimal approvedEvents;
    private BigDecimal rejectedEvents;
    private BigDecimal eventsThisMonth;
    private BigDecimal eventGrowthRate;
    
    // Department Analytics
    private Map<String, Long> usersByDepartment;
    private Map<String, Long> eventsByDepartment;
    private List<DepartmentStatsDTO> departmentDetails;
    
    // Recent Activities
    private BigDecimal todayRegistrations;
    private BigDecimal todayEventCreations;
    private BigDecimal pendingFeedbackReviews;
    private BigDecimal pendingMediaReviews;
    
    // System Alerts
    private List<SystemAlertDTO> systemAlerts;
    private BigDecimal criticalAlerts;
    
    // Performance Metrics
    private BigDecimal averageEventRating;
    private BigDecimal totalRegistrations;
    private BigDecimal completedEvents;
    private BigDecimal certificatesIssued;
    
    // Charts Data
    private List<ChartDataDTO> userRegistrationChart;
    private List<ChartDataDTO> eventCreationChart;
    private List<ChartDataDTO> departmentDistributionChart;
    
    private LocalDateTime lastUpdated;
}