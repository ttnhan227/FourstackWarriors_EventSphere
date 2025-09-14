package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.admin.*;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.admin.AdminFeedbackRepository;
import fpt.aptech.eventsphere.repositories.admin.AdminUserRepository;
import fpt.aptech.eventsphere.services.Admin.AdminDashboardService;
import fpt.aptech.eventsphere.services.Admin.EventManagementService;
import fpt.aptech.eventsphere.services.Admin.UserManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {
    @Autowired
    private final AdminUserRepository adminUserRepository;

    @Autowired
    private final AdminFeedbackRepository adminFeedbackRepository;

    @Autowired
    private final AdminDashboardService adminDashboardService;

    @Autowired
    private final EventManagementService eventManagementService;

    @Autowired
    private UserManagementService userManagementService;

    public AdminController(
            AdminDashboardService adminDashboardService,
            UserManagementService userManagementService,
            EventManagementService eventManagementService,
            AdminUserRepository adminUserRepository,
            AdminFeedbackRepository adminFeedbackRepository) {
        this.adminDashboardService = adminDashboardService;
        this.userManagementService = userManagementService;
        this.eventManagementService = eventManagementService;
        this.adminUserRepository = adminUserRepository;
        this.adminFeedbackRepository = adminFeedbackRepository;
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


    @GetMapping("/feedback")
    public String feedbackManagement(Model model) {
        model.addAttribute("title", "Feedback Management");
        return "admin/feedback";
    }

    @GetMapping("/events")
    public String events(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String organizerName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Model model) {

        try {
            // Log incoming request
            log.info("Processing events request - page: {}, size: {}, keyword: '{}', category: '{}', organizerName: '{}', status: '{}', sortBy: '{}', sortDirection: '{}'",
                    page, size, keyword, category, organizerName, status, sortBy, sortDirection);

            // Ensure sortBy has a default value
            String safeSortBy = (sortBy == null || sortBy.isEmpty()) ? "startDate" : sortBy;

            // Create search request
            EventSearchRequestDTO searchRequest = new EventSearchRequestDTO();
            searchRequest.setPage(page);
            searchRequest.setSize(size);
            searchRequest.setKeyword(keyword);
            searchRequest.setCategory(category);
            searchRequest.setOrganizerName(organizerName);
            searchRequest.setStatus(status);
            searchRequest.setSortBy(safeSortBy);
            searchRequest.setSortDirection(sortDirection);

            // Get paginated events
            Page<EventManagementDTO> eventsPage = eventManagementService.searchAndSortEvents(searchRequest);

            // Get event statistics
            Map<String, Long> eventStats = eventManagementService.getEventStatistics();

            // Add attributes to model with default values
            model.addAttribute("title", "Event Management");
            model.addAttribute("events", eventsPage != null ? eventsPage.getContent() : List.of());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", eventsPage != null ? eventsPage.getTotalElements() : 0);
            model.addAttribute("totalPages", eventsPage != null ? eventsPage.getTotalPages() : 1);
            model.addAttribute("pageSize", size > 0 ? size : 10);

            // Add search parameters to model
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("organizerName", organizerName);
            model.addAttribute("status", status);
            model.addAttribute("sortBy", safeSortBy);
            model.addAttribute("sortDirection", sortDirection);

            // Add statistics to model
            model.addAttribute("totalEvents", eventStats.get("totalEvents"));
            model.addAttribute("pendingEvents", eventStats.get("pendingEvents"));
            model.addAttribute("approvedEvents", eventStats.get("approvedEvents"));
            model.addAttribute("rejectedEvents", eventStats.get("rejectedEvents"));

            // Get all categories for filter dropdown
            List<String> categories = eventManagementService.getAllEventCategories();
            model.addAttribute("categories", categories);

            // Add status options for the filter
            model.addAttribute("statusOptions", Events.EventStatus.values());

            log.info("Successfully processed events request. Found {} events.", eventsPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error processing events request: {}", e.getMessage(), e);
            log.error("Error loading events: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading events: " + e.getMessage());
            model.addAttribute("events", List.of());
            model.addAttribute("categories", List.of());
        }

        return "admin/events";
    }

    @GetMapping("/events/{id}")
    public String viewEventDetails(@PathVariable Integer id, Model model) {
        try {
            log.info("Fetching event details for ID: {}", id);
            EventManagementDTO event = eventManagementService.getEventById(id);
            if (event == null) {
                log.warn("Event not found with ID: {}", id);
                model.addAttribute("error", "Event not found with ID: " + id);
                return "redirect:/admin/events";
            }
            log.debug("Found event: {} (ID: {})", event.getName(), event.getId());
            model.addAttribute("event", event);
            model.addAttribute("title", "Event Details - " + event.getName());
            return "admin/events-detail";
        } catch (Exception e) {
            log.error("Error fetching event details for ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error loading event details: " + e.getMessage());
            return "redirect:/admin/events";
        }
    }


    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Integer id, Model model) {
        try {
            log.info("Fetching event for editing - ID: {}", id);
            EventManagementDTO event = eventManagementService.getEventById(id);
            if (event == null) {
                log.warn("Event not found for editing - ID: {}", id);
                model.addAttribute("error", "Event not found with ID: " + id);
                return "redirect:/admin/events";
            }
            
            log.debug("Found event for editing: {} (ID: {})", event.getName(), event.getId());
            model.addAttribute("event", event);
            model.addAttribute("title", "Edit Event - " + event.getName());
            model.addAttribute("categories", eventManagementService.getAllEventCategories());
            model.addAttribute("statusOptions", Events.EventStatus.values());
            
            return "admin/events-edit";
        } catch (Exception e) {
            log.error("Error fetching event for editing - ID: {} - Error: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error loading event for editing: " + e.getMessage());
            return "redirect:/admin/events";
        }
    }
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        try {
            log.info("Configuring upload directory: {}", uploadDir);
            Path uploadPath = Paths.get(uploadDir, "events");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            } else {
                log.info("Using existing upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", e.getMessage(), e);
        }
    }
    
    @PostMapping("/events/{id}/update")
    public String updateEventDetails(
            @PathVariable int id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(required = false) String status,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Handle file upload if a new image is provided
            String imageUrl = handleFileUpload(imageFile);
            
            // Update event details with the new image URL
            boolean success = eventManagementService.updateEventDetails(
                    id, title, description, category, startDate, endDate, imageUrl);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Event details updated successfully");
                return "redirect:/admin/events/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update event details. Please try again.");
                return "redirect:/admin/events/" + id + "/edit";
            }
        } catch (Exception e) {
            log.error("Error updating event details: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating event: " + e.getMessage());
            return "redirect:/admin/events/" + id + "/edit";
        }
    }
    
    /**
     * Handles file upload and returns the new filename if successful
     */
    private String handleFileUpload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        // Clean and validate the filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("Invalid file name");
        }
        
        // Extract file extension
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        
        // Generate a unique filename
        String newFilename = "event_" + System.currentTimeMillis() + fileExtension;
        
        // Use the configured upload directory with 'events' subfolder
        Path uploadPath = Paths.get(uploadDir, "events");
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
        
        // Save the file
        Path filePath = uploadPath.resolve(newFilename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        log.info("Successfully uploaded file: {}", filePath);
        return "events/" + newFilename;
    }
}
