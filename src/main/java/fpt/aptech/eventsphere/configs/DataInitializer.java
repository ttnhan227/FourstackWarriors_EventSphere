package fpt.aptech.eventsphere.configs;

import fpt.aptech.eventsphere.models.*;
import fpt.aptech.eventsphere.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n===== Starting Data Initialization =====");

        // Initialize roles if none exist
        long roleCount = roleRepository.count();
        System.out.println("Found " + roleCount + " existing roles");
        if (roleCount == 0) {
            System.out.println("Initializing roles...");
            initializeRoles();
            System.out.println("Roles initialized");
        }

        // Initialize users if none exist
        long userCount = userRepository.count();
        System.out.println("Found " + userCount + " existing users");
        if (userCount == 0) {
            System.out.println("Initializing users...");
            initializeUsers();
            System.out.println("Users initialized");
        }

        // Initialize events
        System.out.println("Initializing events...");
        initializeEvents();

        jdbcTemplate.execute("ALTER TABLE events ADD COLUMN IF NOT EXISTS reminder_sent BOOLEAN");
        jdbcTemplate.execute("UPDATE events SET reminder_sent = FALSE WHERE reminder_sent IS NULL");
        jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN reminder_sent SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN reminder_sent SET DEFAULT FALSE");

        fixSeatsBooked();

        System.out.println("===== Data Initialization Complete =====\n");
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
                "Bùi Công Tú",
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

    private void initializeEvents() {
        long eventCount = eventRepository.count();
        System.out.println("Found " + eventCount + " existing events");
        boolean isNewEvents = eventCount == 0;
        if (isNewEvents) {
            // Get organizer user
            Users organizer = userRepository.findByEmail("organizer1@fpt.edu.vn")
                    .orElseThrow(() -> new RuntimeException("Organizer user not found"));

            // Create or get venue
            Venues venue = venueRepository.findByName("FPT University HCM");
            if (venue == null) {
                venue = new Venues();
                venue.setName("FPT University HCM");
                venue = venueRepository.save(venue);
            }

            // Create and save events one by one to ensure proper relationship mapping
            Events event1 = createEvent(
                    "Tech Conference 2023",
                    "Annual technology conference featuring the latest in AI, Cloud, and Software Development",
                    "TECH",
                    LocalDateTime.now().plusDays(30),
                    LocalDateTime.now().plusDays(30).plusHours(8),
                    venue,
                    organizer,
                    200,
                    true
            );
            event1 = eventRepository.save(event1);

            Events event2 = createEvent(
                    "Startup Pitch Night",
                    "Local startups pitch their ideas to investors and industry experts",
                    "BUSINESS",
                    LocalDateTime.now().plusDays(45),
                    LocalDateTime.now().plusDays(45).plusHours(6),
                    venue,
                    organizer,
                    150,
                    true
            );
            event2 = eventRepository.save(event2);

            Events event3 = createEvent(
                    "Blockchain Workshop",
                    "Hands-on workshop about blockchain technology and smart contracts",
                    "WORKSHOP",
                    LocalDateTime.now().plusDays(60),
                    LocalDateTime.now().plusDays(60).plusHours(4),
                    venue,
                    organizer,
                    50,
                    false
            );
            event3 = eventRepository.save(event3);

            Events event4 = createEvent(
                    "Past Tech Meetup",
                    "A meetup for tech enthusiasts that already happened.",
                    "MEETUP",
                    LocalDateTime.now().minusDays(10),
                    LocalDateTime.now().minusDays(10).plusHours(3),
                    venue,
                    organizer,
                    75,
                    false
            );
            event4 = eventRepository.save(event4);

            Events event5 = createEvent(
                    "Archived Webinar",
                    "An online webinar that was recorded and archived.",
                    "WEBINAR",
                    LocalDateTime.now().minusDays(20),
                    LocalDateTime.now().minusDays(20).plusHours(2),
                    venue,
                    organizer,
                    300,
                    true
            );
            event5 = eventRepository.save(event5);
            System.out.println("Created 5 sample events");

            // Get participant users for notifications
            System.out.println("Looking up participant users...");
            try {
                Users participant1 = userRepository.findByEmail("student1@fpt.edu.vn")
                        .orElseThrow(() -> new RuntimeException("Participant user 1 not found"));
                Users participant2 = userRepository.findByEmail("student2@fpt.edu.vn")
                        .orElseThrow(() -> new RuntimeException("Participant user 2 not found"));

                System.out.println("Seeding notifications...");
                seedNotifications(participant1, participant2, event1, event2);
                System.out.println("Notifications seeded successfully");
            } catch (Exception e) {
                System.err.println("Error during notification seeding: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void seedNotifications(Users participant1, Users participant2, Events event1, Events event2) {
        // Create sample notifications
        List<Notification> notifications = List.of(
                // Upcoming event reminder
                Notification.builder()
                        .user(participant1)
                        .title("Upcoming: " + event1.getTitle())
                        .message("Don't forget! " + event1.getTitle() + " is starting in 24 hours.")
                        .type(Notification.NotificationType.EVENT_REMINDER)
                        .relatedEntityId((long) event1.getEventId())
                        .relatedEntityType("EVENT")
                        .actionUrl("/events/" + event1.getEventId())
                        .build(),

                // Registration confirmation
                Notification.builder()
                        .user(participant1)
                        .title("Registration Confirmed")
                        .message("Your registration for " + event2.getTitle() + " has been confirmed.")
                        .type(Notification.NotificationType.EVENT_REGISTRATION)
                        .relatedEntityId((long) event2.getEventId())
                        .relatedEntityType("EVENT")
                        .actionUrl("/events/" + event2.getEventId())
                        .build(),

                // System announcement
                Notification.builder()
                        .user(participant2)
                        .title("Welcome to EventSphere!")
                        .message("Thank you for joining EventSphere. Start exploring events now!")
                        .type(Notification.NotificationType.SYSTEM_ALERT)
                        .actionUrl("/events")
                        .build(),

                // Event update
                Notification.builder()
                        .user(participant1)
                        .title("Event Update: " + event1.getTitle())
                        .message("The venue for " + event1.getTitle() + " has been updated to Room B2.1.")
                        .type(Notification.NotificationType.EVENT_UPDATED)
                        .relatedEntityId((long) event1.getEventId())
                        .relatedEntityType("EVENT")
                        .actionUrl("/events/" + event1.getEventId())
                        .build()
        );

        notificationRepository.saveAll(notifications);
        System.out.println("Created " + notifications.size() + " sample notifications");
    }

    private Events createEvent(String title, String description, String category,
                               LocalDateTime startDate, LocalDateTime endDate,
                               Venues venue, Users organizer,
                               int totalSeats, boolean waitlistEnabled) {
        // First create the event
        Events event = new Events();
        event.setTitle(title);
        event.setDescription(description);
        event.setCategory(category);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setReminderSent(false);

        // Create and set event seating
        EventSeating seating = new EventSeating();
        seating.setTotalSeats(totalSeats);
        seating.setSeatsBooked(0);
        seating.setWaitlistEnabled(waitlistEnabled);

        // Set the bidirectional relationship
        // This will automatically set the event_id in the event_seating table
        event.setEventSeating(seating);  // This also sets seating.event = event

        return event;
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

    @Transactional
    public void fixSeatsBooked() {
        // Query to get event IDs with seats_booked = 0
        String selectEventsSql = "SELECT event_id FROM event_seating WHERE seats_booked = 0";
        List<Integer> eventIds = jdbcTemplate.queryForList(selectEventsSql, Integer.class);

        // For each event, count CONFIRMED registrations and update seats_booked
        String countConfirmedSql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CONFIRMED'";
        String updateSeatsSql = "UPDATE event_seating SET seats_booked = ? WHERE event_id = ?";

        for (Integer eventId : eventIds) {
            // Get count of CONFIRMED registrations
            Integer confirmedCount = jdbcTemplate.queryForObject(countConfirmedSql, Integer.class, eventId);
            // Update seats_booked
            jdbcTemplate.update(updateSeatsSql, confirmedCount, eventId);
        }
    }
}
