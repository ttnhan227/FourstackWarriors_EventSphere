package fpt.aptech.eventsphere.dto.admin;

import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Data
public class EventSearchRequestDTO {
    private static final String DEFAULT_SORT_FIELD = "startDate";
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private String keyword;
    private String category;
    private String organizerName;
    private String status;
    private String sortBy = DEFAULT_SORT_FIELD;
    private String sortDirection = DEFAULT_SORT_DIRECTION;
    private int page = DEFAULT_PAGE;
    private int size = DEFAULT_SIZE;

    public boolean hasKeyword() {
        return StringUtils.hasText(keyword);
    }

    public boolean hasCategory() {
        return StringUtils.hasText(category) && !"all".equalsIgnoreCase(category);
    }

    public boolean hasOrganizerName() {
        return StringUtils.hasText(organizerName);
    }
    
    public String getStatus() {
        return status;
    }
    
    public boolean hasStatus() {
        return StringUtils.hasText(status) && !"all".equalsIgnoreCase(status);
    }

    public Sort getSort() {
        // Default sort field is startDate
        String sortField = StringUtils.hasText(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        
        // Validate sort field to prevent SQL injection
        if (!isValidSortField(sortField)) {
            sortField = DEFAULT_SORT_FIELD;
        }
        
        // Get sort direction, default to ascending if not specified
        String direction = StringUtils.hasText(sortDirection) ? sortDirection : DEFAULT_SORT_DIRECTION;
        boolean isDesc = "desc".equalsIgnoreCase(direction);
        
        // Create sort object
        return isDesc 
            ? Sort.by(sortField).descending() 
            : Sort.by(sortField).ascending();
    }
    
    private boolean isValidSortField(String field) {
        if (field == null) {
            return false;
        }
        // List of allowed sort fields
        Set<String> allowedFields = new HashSet<>(Arrays.asList(
            "startDate", "title", "category", "status"
        ));
        return allowedFields.contains(field);
    }
    
    public int getPage() {
        return Math.max(DEFAULT_PAGE, page);
    }
    
    public int getSize() {
        return (size < 1 || size > MAX_PAGE_SIZE) ? DEFAULT_SIZE : size;
    }
}
