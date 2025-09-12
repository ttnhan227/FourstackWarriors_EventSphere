package fpt.aptech.eventsphere.services.Admin;

import fpt.aptech.eventsphere.dto.admin.UserManagementDTO;
import fpt.aptech.eventsphere.dto.admin.UserSearchRequestDTO;
import fpt.aptech.eventsphere.repositories.RoleRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserManagementService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public Page<UserManagementDTO> searchAndSortUsers(UserSearchRequestDTO searchRequest) {
        List<Object[]> results = userRepository.searchUsersForManagement(
                searchRequest.hasKeyword() ? searchRequest.getKeyword() : null,
                searchRequest.hasDepartment() ? searchRequest.getDepartment() : null,
                searchRequest.hasRole() ? searchRequest.getRole() : null,
                searchRequest.hasActiveFilter() ? searchRequest.getIsActive() : null
        );

        // Convert Object[] to UserManagementDTO
        List<UserManagementDTO> users = convertToUserManagementDTO(results);

        // Enrich with roles data
        users = enrichUsersWithRoles(users);

        // Apply sorting
        users = applySorting(users, searchRequest.getSortBy(), searchRequest.getSortDirection());

        // Apply pagination
        int start = searchRequest.getPage() * searchRequest.getSize();
        int end = Math.min(start + searchRequest.getSize(), users.size());

        List<UserManagementDTO> pagedUsers = users.subList(start, end);

        return new PageImpl<>(pagedUsers,
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()),
                users.size());
    }

    private List<UserManagementDTO> convertToUserManagementDTO(List<Object[]> results) {
        return results.stream().map(row -> {
            UserManagementDTO dto = new UserManagementDTO();
            dto.setUserId((Integer) row[0]);
            dto.setEmail((String) row[1]);
            dto.setFullName((String) row[2]);
            dto.setPhone((String) row[3]);
            dto.setDepartment((String) row[4]);
            dto.setEnrollmentNo((String) row[5]);
            dto.setActive((Boolean) row[6]);
            dto.setDeleted((Boolean) row[7]);
            dto.setGoogleId((String) row[8]);
            dto.setCreatedAt((java.time.LocalDateTime) row[9]);
            dto.setAvatar((String) row[10]);
            dto.setAddress((String) row[11]);

            // Initialize empty values for statistics
            dto.setEventsAttended(java.math.BigDecimal.ZERO);
            dto.setEventsOrganized(java.math.BigDecimal.ZERO);
            dto.setAverageRating(java.math.BigDecimal.ZERO);

            return dto;
        }).collect(Collectors.toList());
    }

    private List<UserManagementDTO> enrichUsersWithRoles(List<UserManagementDTO> users) {
        for (UserManagementDTO user : users) {
            // Get roles for each user - now returns List<String> directly
            List<String> roles = userRepository.findRolesByUserId(user.getUserId());
            user.setRoles(roles);
        }
        return users;
    }

    public List<String> getAllDepartments() {
        return userRepository.findAllDepartments();
    }

    public List<String> getAllRoles() {
        return userRepository.findAllRoles();
    }

    private List<UserManagementDTO> applySorting(List<UserManagementDTO> users, String sortBy, String sortDirection) {
        Comparator<UserManagementDTO> comparator;

        switch (sortBy.toLowerCase()) {
            case "email":
                comparator = Comparator.comparing(u -> u.getEmail() != null ? u.getEmail().toLowerCase() : "");
                break;
            case "fullname":
                comparator = Comparator.comparing(u -> u.getFullName() != null ? u.getFullName().toLowerCase() : "");
                break;
            case "department":
                comparator = Comparator.comparing(u -> u.getDepartment() != null ? u.getDepartment().toLowerCase() : "");
                break;
            case "createdat":
                comparator = Comparator.comparing(u -> u.getCreatedAt() != null ? u.getCreatedAt() : java.time.LocalDateTime.MIN);
                break;
            case "isactive":
                comparator = Comparator.comparing(UserManagementDTO::isActive);
                break;
            case "roles":
                comparator = Comparator.comparing(u -> u.getRoles() != null && !u.getRoles().isEmpty() ? u.getRoles().get(0) : "");
                break;
            default: // userId
                comparator = Comparator.comparing(UserManagementDTO::getUserId);
                break;
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return users.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    public long getTotalUserCount() {
        return userRepository.countByIsDeletedFalseAndNotAdmin();
    }

    public long getActiveUserCount() {
        return userRepository.countByIsActiveTrueAndIsDeletedFalseAndNotAdmin();
    }

    public long getInactiveUserCount() {
        return userRepository.countByIsActiveFalseAndIsDeletedFalseAndNotAdmin();
    }


}