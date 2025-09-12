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

    public BigDecimal getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(BigDecimal totalUsers) {
        this.totalUsers = totalUsers;
    }

    public BigDecimal getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(BigDecimal activeUsers) {
        this.activeUsers = activeUsers;
    }

    public BigDecimal getSuspendedUsers() {
        return suspendedUsers;
    }

    public void setSuspendedUsers(BigDecimal suspendedUsers) {
        this.suspendedUsers = suspendedUsers;
    }

    public BigDecimal getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    public void setNewUsersThisMonth(BigDecimal newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }

    public BigDecimal getUserGrowthRate() {
        return userGrowthRate;
    }

    public void setUserGrowthRate(BigDecimal userGrowthRate) {
        this.userGrowthRate = userGrowthRate;
    }

    public BigDecimal getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(BigDecimal totalEvents) {
        this.totalEvents = totalEvents;
    }

    public BigDecimal getPendingEvents() {
        return pendingEvents;
    }

    public void setPendingEvents(BigDecimal pendingEvents) {
        this.pendingEvents = pendingEvents;
    }

    public BigDecimal getApprovedEvents() {
        return approvedEvents;
    }

    public void setApprovedEvents(BigDecimal approvedEvents) {
        this.approvedEvents = approvedEvents;
    }

    public BigDecimal getRejectedEvents() {
        return rejectedEvents;
    }

    public void setRejectedEvents(BigDecimal rejectedEvents) {
        this.rejectedEvents = rejectedEvents;
    }

    public BigDecimal getEventsThisMonth() {
        return eventsThisMonth;
    }

    public void setEventsThisMonth(BigDecimal eventsThisMonth) {
        this.eventsThisMonth = eventsThisMonth;
    }

    public BigDecimal getEventGrowthRate() {
        return eventGrowthRate;
    }

    public void setEventGrowthRate(BigDecimal eventGrowthRate) {
        this.eventGrowthRate = eventGrowthRate;
    }

    public Map<String, Long> getUsersByDepartment() {
        return usersByDepartment;
    }

    public void setUsersByDepartment(Map<String, Long> usersByDepartment) {
        this.usersByDepartment = usersByDepartment;
    }

    public Map<String, Long> getEventsByDepartment() {
        return eventsByDepartment;
    }

    public void setEventsByDepartment(Map<String, Long> eventsByDepartment) {
        this.eventsByDepartment = eventsByDepartment;
    }

    public List<DepartmentStatsDTO> getDepartmentDetails() {
        return departmentDetails;
    }

    public void setDepartmentDetails(List<DepartmentStatsDTO> departmentDetails) {
        this.departmentDetails = departmentDetails;
    }

    public BigDecimal getTodayRegistrations() {
        return todayRegistrations;
    }

    public void setTodayRegistrations(BigDecimal todayRegistrations) {
        this.todayRegistrations = todayRegistrations;
    }

    public BigDecimal getTodayEventCreations() {
        return todayEventCreations;
    }

    public void setTodayEventCreations(BigDecimal todayEventCreations) {
        this.todayEventCreations = todayEventCreations;
    }

    public BigDecimal getPendingFeedbackReviews() {
        return pendingFeedbackReviews;
    }

    public void setPendingFeedbackReviews(BigDecimal pendingFeedbackReviews) {
        this.pendingFeedbackReviews = pendingFeedbackReviews;
    }

    public BigDecimal getPendingMediaReviews() {
        return pendingMediaReviews;
    }

    public void setPendingMediaReviews(BigDecimal pendingMediaReviews) {
        this.pendingMediaReviews = pendingMediaReviews;
    }

    public List<SystemAlertDTO> getSystemAlerts() {
        return systemAlerts;
    }

    public void setSystemAlerts(List<SystemAlertDTO> systemAlerts) {
        this.systemAlerts = systemAlerts;
    }

    public BigDecimal getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(BigDecimal criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public BigDecimal getAverageEventRating() {
        return averageEventRating;
    }

    public void setAverageEventRating(BigDecimal averageEventRating) {
        this.averageEventRating = averageEventRating;
    }

    public BigDecimal getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(BigDecimal totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public BigDecimal getCompletedEvents() {
        return completedEvents;
    }

    public void setCompletedEvents(BigDecimal completedEvents) {
        this.completedEvents = completedEvents;
    }

    public BigDecimal getCertificatesIssued() {
        return certificatesIssued;
    }

    public void setCertificatesIssued(BigDecimal certificatesIssued) {
        this.certificatesIssued = certificatesIssued;
    }

    public List<ChartDataDTO> getUserRegistrationChart() {
        return userRegistrationChart;
    }

    public void setUserRegistrationChart(List<ChartDataDTO> userRegistrationChart) {
        this.userRegistrationChart = userRegistrationChart;
    }

    public List<ChartDataDTO> getEventCreationChart() {
        return eventCreationChart;
    }

    public void setEventCreationChart(List<ChartDataDTO> eventCreationChart) {
        this.eventCreationChart = eventCreationChart;
    }

    public List<ChartDataDTO> getDepartmentDistributionChart() {
        return departmentDistributionChart;
    }

    public void setDepartmentDistributionChart(List<ChartDataDTO> departmentDistributionChart) {
        this.departmentDistributionChart = departmentDistributionChart;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}