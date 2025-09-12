package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.admin.AdminDashboardDTO;
import fpt.aptech.eventsphere.dto.admin.UserManagementDTO;
import fpt.aptech.eventsphere.dto.admin.UserSearchRequestDTO;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.admin.*;
import fpt.aptech.eventsphere.repositories.admin.AdminEventRepository;
import fpt.aptech.eventsphere.services.Admin.AdminDashboardService;
import fpt.aptech.eventsphere.services.Admin.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private final AdminUserRepository adminUserRepository;

    @Autowired
    private final AdminEventRepository adminEventRepository;

    @Autowired
    private final AdminFeedbackRepository adminFeedbackRepository;

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private UserManagementService userManagementService;


    public AdminController(
            AdminUserRepository adminUserRepository,
            AdminEventRepository adminEventRepository, 
            AdminFeedbackRepository adminFeedbackRepository,
            AdminDashboardService adminDashboardService) {
        this.adminUserRepository = adminUserRepository;
        this.adminEventRepository = adminEventRepository;
        this.adminFeedbackRepository = adminFeedbackRepository;
        this.adminDashboardService = adminDashboardService;
        this.userManagementService = userManagementService;
    }

    @GetMapping("/index")
    public String adminDashboard(Model model) {
        try {
            AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData();

            // Debug log để kiểm tra dữ liệu
            System.out.println("=== DEBUG DASHBOARD DATA ===");
            System.out.println("Total Users: " + dashboardData.getTotalUsers());
            System.out.println("Active Users: " + dashboardData.getActiveUsers());
            System.out.println("Suspended Users: " + dashboardData.getSuspendedUsers());
            System.out.println("New Users This Month: " + dashboardData.getNewUsersThisMonth());

            model.addAttribute("title", "Admin Dashboard");
            model.addAttribute("dashboard", dashboardData);


            // thống kê người dùng
            model.addAttribute("totalUsers", dashboardData.getTotalUsers());
            model.addAttribute("activeUsers", dashboardData.getActiveUsers());
            model.addAttribute("suspendedUsers", dashboardData.getSuspendedUsers());
            model.addAttribute("newUsersThisMonth", dashboardData.getNewUsersThisMonth());
            model.addAttribute("userGrowthRate", dashboardData.getUserGrowthRate());

            // thống kê sự kiện
            model.addAttribute("totalEvents", dashboardData.getTotalEvents());
            model.addAttribute("approvedEvents", dashboardData.getApprovedEvents());
            model.addAttribute("pendingEvents", dashboardData.getPendingEvents());
            model.addAttribute("rejectedEvents", dashboardData.getRejectedEvents());
            model.addAttribute("eventsThisMonth", dashboardData.getEventsThisMonth());
            model.addAttribute("eventGrowthRate", dashboardData.getEventGrowthRate());

            // phòng ban hiệu suất cao
            model.addAttribute("departmentDetails", dashboardData.getDepartmentDetails());
            model.addAttribute("usersByDepartment", dashboardData.getUsersByDepartment());
            model.addAttribute("eventsByDepartment", dashboardData.getEventsByDepartment());

            // Cảnh báo hệ thống
            model.addAttribute("systemAlerts", dashboardData.getSystemAlerts());
            model.addAttribute("criticalAlerts", dashboardData.getCriticalAlerts());

            // hoạt động gần đây
            model.addAttribute("todayRegistrations", dashboardData.getTodayRegistrations());
            model.addAttribute("todayEventCreations", dashboardData.getTodayEventCreations());
            model.addAttribute("pendingFeedbackReviews", dashboardData.getPendingFeedbackReviews());

            // Chỉ số hiệu suất
            model.addAttribute("averageEventRating", dashboardData.getAverageEventRating());
            model.addAttribute("totalRegistrations", dashboardData.getTotalRegistrations());
            model.addAttribute("completedEvents", dashboardData.getCompletedEvents());
            model.addAttribute("certificatesIssued", dashboardData.getCertificatesIssued());

            // chart data
            model.addAttribute("userRegistrationChart", dashboardData.getUserRegistrationChart());
            model.addAttribute("eventCreationChart", dashboardData.getEventCreationChart());
            model.addAttribute("departmentDistributionChart", dashboardData.getDepartmentDistributionChart());

            model.addAttribute("lastUpdated", dashboardData.getLastUpdated());

        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            model.addAttribute("title", "Admin Dashboard");
            model.addAttribute("totalUsers", adminUserRepository.countByIsActiveTrueAndIsDeletedFalse());
            model.addAttribute("totalEvents", adminEventRepository.countUpcomingEvents());
            model.addAttribute("completedEvents", adminEventRepository.countCompletedEvents());
            model.addAttribute("averageRating", adminFeedbackRepository.getAverageRating());
        }
        return "admin/index";
    }

    @GetMapping("/users")
    public String userManagement(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "all") String department,
            @RequestParam(defaultValue = "all") String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        try {
            // Create search request
            UserSearchRequestDTO searchRequest = new UserSearchRequestDTO();
            searchRequest.setKeyword(keyword);
            searchRequest.setDepartment(department);
            searchRequest.setRole(role);

            // Handle status filter
            if ("active".equals(status)) {
                searchRequest.setIsActive(true);
            } else if ("inactive".equals(status)) {
                searchRequest.setIsActive(false);
            }

            searchRequest.setSortBy(sortBy);
            searchRequest.setSortDirection(sortDirection);
            searchRequest.setPage(page);
            searchRequest.setSize(size);

            // Get paginated results
            Page<UserManagementDTO> usersPage = userManagementService.searchAndSortUsers(searchRequest);

            // Get departments and roles for dropdowns
            List<String> departments = userManagementService.getAllDepartments();
            List<String> roles = userManagementService.getAllRoles();

            model.addAttribute("title", "User Management");
            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("departments", departments);
            model.addAttribute("roles", roles);

            // Pagination info
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalItems", usersPage.getTotalElements());
            model.addAttribute("pageSize", size);

            // Search parameters for maintaining state
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedDepartment", department);
            model.addAttribute("selectedRole", role);
            model.addAttribute("selectedStatus", status != null ? status : "all");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDirection", sortDirection);

            // Statistics
            model.addAttribute("totalUsers", userManagementService.getTotalUserCount());
            model.addAttribute("activeUsers", userManagementService.getActiveUserCount());
            model.addAttribute("inactiveUsers", userManagementService.getInactiveUserCount());

        } catch (Exception e) {
            model.addAttribute("error", "Error loading list users: " + e.getMessage());
            model.addAttribute("users", List.of());
            model.addAttribute("departments", List.of());
            model.addAttribute("roles", List.of());
        }

        return "admin/users";
    }


    @PostMapping("/users/{id}/toggle-status")
    @ResponseBody
    public Map<String, Object> toggleUserStatus(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== DEBUG TOGGLE USER STATUS ===");
            System.out.println("Received userId: " + id);

            Users user = adminUserRepository.findById(id).orElse(null);
            if (user != null && !user.isDeleted()) {
                System.out.println("Found user: " + user.getEmail());
                System.out.println("Current status: " + user.isActive());

                boolean oldStatus = user.isActive();
                user.setActive(!user.isActive());
                adminUserRepository.save(user);

                System.out.println("New status: " + user.isActive());
                System.out.println("Status changed from " + oldStatus + " to " + user.isActive());

                response.put("success", true);
                response.put("newStatus", user.isActive());
                response.put("message", user.isActive() ? "User activated successfully" : "User deactivated successfully");
            } else {
                System.out.println("User not found or deleted for id: " + id);
                response.put("success", false);
                response.put("message", "User not found or deleted");
            }
        } catch (Exception e) {
            System.err.println("Error in toggleUserStatus: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating user status: " + e.getMessage());
        }

        System.out.println("Returning response: " + response);
        return response;
    }

    @GetMapping("/events")
    public String eventManagement(Model model) {
        model.addAttribute("title", "Event Management");
        model.addAttribute("upcomingEvents", adminEventRepository.findUpcomingEvents(LocalDateTime.now()));
        model.addAttribute("pastEvents", adminEventRepository.findPastEvents(LocalDateTime.now()));
        return "admin/events";
    }

    @GetMapping("/feedback")
    public String feedbackManagement(Model model) {
        model.addAttribute("title", "Feedback Management");
        return "admin/feedback";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("title", "Reports");
        return "admin/reports";
    }

}
