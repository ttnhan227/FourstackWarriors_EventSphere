package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.admin.*;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.models.admin.EventsModel;
import fpt.aptech.eventsphere.models.admin.EventsModel.Status;
import fpt.aptech.eventsphere.repositories.admin.*;
import fpt.aptech.eventsphere.repositories.admin.AdminEventRepository;
import fpt.aptech.eventsphere.services.Admin.AdminDashboardService;
import fpt.aptech.eventsphere.services.Admin.EventModerationService;
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
    private final AdminEventModelRepository adminEventModelRepository;


    @Autowired
    private AdminDashboardService adminDashboardService;


    @Autowired
    private EventModerationService eventServiceAnhTu;


    @Autowired
    private UserManagementService userManagementService;
    @Autowired
    private EventModerationService eventModerationService;


    public AdminController(
            AdminUserRepository adminUserRepository,
            AdminEventRepository adminEventRepository, 
            AdminFeedbackRepository adminFeedbackRepository,
            AdminDashboardService adminDashboardService,
            AdminEventModelRepository adminEventModelRepository) {
        this.adminUserRepository = adminUserRepository;
        this.adminEventRepository = adminEventRepository;
        this.adminFeedbackRepository = adminFeedbackRepository;
        this.adminDashboardService = adminDashboardService;
        this.adminEventModelRepository = adminEventModelRepository;

    }

    @GetMapping("/index")
    public String adminDashboard(Model model) {
        try {
            AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData();

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
            UserSearchRequestDTO searchRequest = new UserSearchRequestDTO();
            searchRequest.setKeyword(keyword);
            searchRequest.setDepartment(department);
            searchRequest.setRole(role);

            if ("active".equals(status)) {
                searchRequest.setIsActive(true);
            } else if ("inactive".equals(status)) {
                searchRequest.setIsActive(false);
            }

            searchRequest.setSortBy(sortBy);
            searchRequest.setSortDirection(sortDirection);
            searchRequest.setPage(page);
            searchRequest.setSize(size);

            Page<UserManagementDTO> usersPage = userManagementService.searchAndSortUsers(searchRequest);

            List<String> departments = userManagementService.getAllDepartments();
            List<String> roles = userManagementService.getAllRoles();

            model.addAttribute("title", "User Management");
            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("departments", departments);
            model.addAttribute("roles", roles);

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalItems", usersPage.getTotalElements());
            model.addAttribute("pageSize", size);

            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedDepartment", department);
            model.addAttribute("selectedRole", role);
            model.addAttribute("selectedStatus", status != null ? status : "all");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDirection", sortDirection);

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

//    @GetMapping("/events")
//    public String eventManagement(Model model) {
//        try{
//            AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData();
//
//            model.addAttribute("title", "Event Management");
//
//            model.addAttribute("totalEvents", dashboardData.getTotalEvents());
//            model.addAttribute("completedEvents", dashboardData.getCompletedEvents());
//            model.addAttribute("eventsThisMonth", dashboardData.getEventsThisMonth());
//            model.addAttribute("eventGrowthRate", dashboardData.getEventGrowthRate());
//
//            model.addAttribute("pendingEvents", dashboardData.getPendingEvents());
//            model.addAttribute("approveEvents", dashboardData.getApprovedEvents());
//            model.addAttribute("rejectEvents", dashboardData.getRejectedEvents());
//
//            model.addAttribute("editEvents", adminEventModelRepository.countByStatus(EventsModel.Status.CHANGE_REQUESTED));
//            model.addAttribute("cancelEvents", adminEventModelRepository.countByStatus(EventsModel.Status.CANCELLED));
//            model.addAttribute("doneEvents", adminEventModelRepository.countByStatus(EventsModel.Status.FINISHED));
//
//            model.addAttribute("upcomingEvents", adminEventRepository.findUpcomingEvents(LocalDateTime.now()));
//            model.addAttribute("pastEvents", adminEventRepository.findPastEvents(LocalDateTime.now()));
//
//            return "admin/event";
//
//        } catch (Exception e) {
//            model.addAttribute("error", "Error loading list events: " + e.getMessage());
//            return "admin/event";
//        }
//    }
@GetMapping("/events")
public String eventManagement(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "all") String category,
        @RequestParam(defaultValue = "") String organizerName,
        @RequestParam(defaultValue = "all") String status,
        @RequestParam(defaultValue = "startDate") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDirection,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Model model) {
    try {
        AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData();

        // Create search request for all events
        EventModelSearchDTO searchRequest = new EventModelSearchDTO();
        searchRequest.setKeyword(keyword.isEmpty() ? null : keyword);
        searchRequest.setStatus(status.equals("all") ? null : Status.valueOf(status.toUpperCase()));
        searchRequest.setCategory(category.equals("all") ? null : category);
        searchRequest.setOrganizerName(organizerName.isEmpty() ? null : organizerName);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        searchRequest.setPage(page);
        searchRequest.setSize(size);

        Page<EventModerationDTO> eventsPage = eventModerationService.searchAndSortAllEvents(searchRequest);



        model.addAttribute("title", "Event Management");
        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("totalItems", eventsPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Search parameters
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("organizerName", organizerName);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);

        // Dashboard statistics
        model.addAttribute("totalEvents", dashboardData.getTotalEvents());
        model.addAttribute("completedEvents", dashboardData.getCompletedEvents());
        model.addAttribute("eventsThisMonth", dashboardData.getEventsThisMonth());
        model.addAttribute("eventGrowthRate", dashboardData.getEventGrowthRate());
        model.addAttribute("pendingEvents", dashboardData.getPendingEvents());
        model.addAttribute("approveEvents", dashboardData.getApprovedEvents());
        model.addAttribute("rejectEvents", dashboardData.getRejectedEvents());
        model.addAttribute("editEvents", adminEventModelRepository.countByStatus(EventsModel.Status.CHANGE_REQUESTED));
        model.addAttribute("cancelEvents", adminEventModelRepository.countByStatus(EventsModel.Status.CANCELLED));
        model.addAttribute("doneEvents", adminEventModelRepository.countByStatus(EventsModel.Status.FINISHED));




        return "admin/event";


    } catch (Exception e) {
        model.addAttribute("error", "Error loading events: " + e.getMessage());
        model.addAttribute("events", List.of());
        model.addAttribute("totalItems", 0);
        return "admin/event";
    }
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

    @GetMapping("/events/pending")
    public String pendingEvents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "") String organizerName,
            @RequestParam(defaultValue = "submitAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            // Load dashboard statistics
            AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData();

            model.addAttribute("totalEvents", dashboardData.getTotalEvents());
            model.addAttribute("pendingEvents", dashboardData.getPendingEvents());
            model.addAttribute("approvedEvents", dashboardData.getApprovedEvents());
            model.addAttribute("rejectedEvents", dashboardData.getRejectedEvents());
            model.addAttribute("completedEvents", dashboardData.getCompletedEvents());

            // Use helper to load event list with status = PENDING
            return handleEventsByStatus(
                    Status.PENDING,
                    keyword,
                    category,
                    organizerName,
                    sortBy,
                    sortDirection,
                    page,
                    size,
                    model,
                    "admin/events-pending"
            );

        } catch (Exception e) {
            model.addAttribute("error", "Error loading pending events: " + e.getMessage());
            model.addAttribute("events", List.of());
            return "admin/events-pending";
        }
    }

    @GetMapping("/events/approved")
    public String approvedEvents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "") String organizerName,
            @RequestParam(defaultValue = "submitAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData();

            model.addAttribute("totalEvents", dashboardData.getTotalEvents());
            model.addAttribute("pendingEvents", dashboardData.getPendingEvents());
            model.addAttribute("approvedEvents", dashboardData.getApprovedEvents());
            model.addAttribute("rejectedEvents", dashboardData.getRejectedEvents());
            model.addAttribute("completedEvents", dashboardData.getCompletedEvents());

            return handleEventsByStatus(
                    Status.APPROVED,
                    keyword,
                    category,
                    organizerName,
                    sortBy,
                    sortDirection,
                    page,
                    size,
                    model,
                    "admin/events-approved"
            );

        } catch (Exception e) {
            model.addAttribute("error", "Error loading approved events: " + e.getMessage());
            model.addAttribute("events", List.of());
            return "admin/events-approved";
        }
    }

    @GetMapping("/events/rejected")
    public String rejectedEvents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "") String organizerName,
            @RequestParam(defaultValue = "submitAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData();

            model.addAttribute("totalEvents", dashboardData.getTotalEvents());
            model.addAttribute("pendingEvents", dashboardData.getPendingEvents());
            model.addAttribute("approvedEvents", dashboardData.getApprovedEvents());
            model.addAttribute("rejectedEvents", dashboardData.getRejectedEvents());
            model.addAttribute("completedEvents", dashboardData.getCompletedEvents());

            return handleEventsByStatus(
                    Status.REJECTED,
                    keyword,
                    category,
                    organizerName,
                    sortBy,
                    sortDirection,
                    page,
                    size,
                    model,
                    "admin/events-rejected"
            );

        } catch (Exception e) {
            model.addAttribute("error", "Error loading rejected events: " + e.getMessage());
            model.addAttribute("events", List.of());
            return "admin/events-rejected";
        }
    }

    @GetMapping("/events/{id}")
    public String viewEventDetails(@PathVariable Integer id, Model model) {
        try {
            Optional<EventModerationDTO> eventOpt = eventModerationService.getEventModerationById(id);
            if (eventOpt.isPresent()) {
                model.addAttribute("event", eventOpt.get());
                model.addAttribute("title", "Event Details");
                return "admin/events-detail";
            } else {
                model.addAttribute("error", "Event not found");
                return "error/404";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading event details: " + e.getMessage());
            return "error/error";
        }
    }


    private String handleEventsByStatus(
            Status status,
            String keyword, String category,
            String organizerName, String sortBy,
            String sortDirection, int page,
            int size, Model model, String s) {
        try {
            EventModelSearchDTO searchRequest = new EventModelSearchDTO();
            searchRequest.setKeyword(keyword);
            searchRequest.setStatus(status);
            searchRequest.setCategory(category);
            searchRequest.setOrganizerName(organizerName);
            searchRequest.setSortBy(sortBy);
            searchRequest.setSortDirection(sortDirection);
            searchRequest.setPage(page);
            searchRequest.setSize(size);

            Page<EventModerationDTO> eventsPage = eventModerationService.searchAndSortAllEvents(searchRequest);
            model.addAttribute("title", getStatusTitle(status));
            model.addAttribute("events", eventsPage.getContent());
//            model.addAttribute("categories", categories);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", eventsPage.getTotalPages());
            model.addAttribute("totalItems", eventsPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("status", status);

            // Search parameters
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("organizerName", organizerName);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDirection", sortDirection);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading event details: " + e.getMessage());
            model.addAttribute("events", List.of());
        }
        return s;
    }

    private String getStatusTitle(Status status) {
        switch (status) {
            case PENDING: return "Pending Events";
            case APPROVED: return "Approved Events";
            case REJECTED: return "Rejected Events";
            case CHANGE_REQUESTED: return "Change Requested Events";
            default: return "Events";
        }
    }

}
