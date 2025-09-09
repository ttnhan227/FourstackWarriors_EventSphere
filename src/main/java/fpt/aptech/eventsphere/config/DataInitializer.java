package fpt.aptech.eventsphere.config;

import fpt.aptech.eventsphere.models.*;
import fpt.aptech.eventsphere.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            initializeRoles();
        }

        if (userRepository.count() == 0) {
            initializeUsers();
        }
    }

    private void initializeRoles() {
        List<Roles> roles = List.of(
                new Roles("ADMIN"),
                new Roles("ORGANIZER"),
                new Roles("PARTICIPANT")
        );

        roleRepository.saveAll(roles);
        System.out.println("Created " + roles.size() + " roles");
    }

    private void initializeUsers() {
        Roles adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();
        Roles organizerRole = roleRepository.findByRoleName("ORGANIZER").orElseThrow();
        Roles participantRole = roleRepository.findByRoleName("PARTICIPANT").orElseThrow();

        // Tạo Admin user
        Users admin = createUser(
                "admin@admin.com",
                "AdminPass123!",
                true,
                List.of(adminRole)
        );

        UserDetails adminDetails = createUserDetails(
                admin,
                "Administrator",
                "+84901234567",
                "IT Department",
                "ADMIN001",
                "/images/avatars/avatars.png",
                "123 Admin Street, Hồ Chí Minh"
        );

        // Tạo Organizer users
        Users organizer1 = createUser(
                "organizer1@fpt.edu.vn",
                "OrgPass123!",
                true,
                List.of(organizerRole)
        );

        UserDetails organizer1Details = createUserDetails(
                organizer1,
                "Administrator Event",
                "+84912345678",
                "Student Affairs",
                "ORG001",
                "/images/avatars/avatar2.png",
                "456 Organizer Ave, TP.HCM"
        );

        Users organizer2 = createUser(
                "organizer2@fpt.edu.vn",
                "OrgPass123!",
                true,
                List.of(organizerRole)
        );

        UserDetails organizer2Details = createUserDetails(
                organizer2,
                "Event Coordinator",
                "+84923456789",
                "Marketing Department",
                "ORG002",
                "/images/avatars/avatar3.png",
                "789 Event Road, Đà Nẵng"
        );

        // Tạo Participant users
        String[] participantNames = {
                "Lê Văn Tham Gia", "Phạm Thị Sinh Viên", "Hoàng Văn Học Tập",
                "Vũ Thị Hoạt Động", "Đỗ Văn Tích Cực"
        };

        String[] phones = {
                "+84934567890", "+84945678901", "+84956789012",
                "+84967890123", "+84978901234"
        };

        String[] enrollments = {
                "SE161234", "SE161235", "SE161236", "SE161237", "SE161238"
        };

        for (int i = 0; i < 5; i++) {
            Users participant = createUser(
                    "student" + (i + 1) + "@fpt.edu.vn",
                    "StudentPass123!",
                    true,
                    List.of(participantRole)
            );

            UserDetails participantDetail = createUserDetails(
                    participant,
                    participantNames[i],
                    phones[i],
                    "Software Engineering",
                    enrollments[i],
                    "/images/avatars/avatar2" + (i + 1) + ".png",
                    "Student Dormitory Block " + (i + 1) + ", FPT University"
            );
        }

        // Tạo một số user chưa active (để test activation)
        Users inactiveUser = createUser(
                "inactive@test.com",
                "TestPass123!",
                false,
                List.of(participantRole)
        );

        UserDetails inactiveDetails = createUserDetails(
                inactiveUser,
                "User Chưa Kích Hoạt",
                "+84987654321",
                "Test Department",
                "TEST001",
                null,
                "Test Address"
        );

        System.out.println("Created initial users");
    }

    private Users createUser(String email, String password, boolean isActive, List<Roles> roles) {
        Users user = new Users();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(isActive);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private UserDetails createUserDetails(Users user, String fullName, String phone,
                                          String department, String enrollmentNo,
                                          String avatar, String address) {
        UserDetails details = new UserDetails();
        details.setUser(user);
        details.setFullName(fullName);
        details.setPhone(phone);
        details.setDepartment(department);
        details.setEnrollmentNo(enrollmentNo);
        details.setAvatar(avatar);
        details.setAddress(address);

        return userDetailsRepository.save(details);
    }
}