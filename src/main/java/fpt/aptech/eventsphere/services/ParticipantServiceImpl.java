package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ParticipantRegistrationDto;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Roles;
import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.ParticipantRepository;
import fpt.aptech.eventsphere.repositories.RoleRepository;
import fpt.aptech.eventsphere.repositories.UserDetailsRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ParticipantServiceImpl(ParticipantRepository participantRepository,
                            UserRepository userRepository,
                            UserDetailsRepository userDetailsRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Events> getUpcomingEvents() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.findUpcomingRegisteredEvents(currentUser.getUserId());
    }

    @Override
    public List<Events> getPastEvents() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.findPastRegisteredEvents(currentUser.getUserId());
    }

    @Override
    public int getTotalRegistrations() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.countRegistrationsByUserId(currentUser.getUserId());
    }

    @Override
    public int getAttendedEventsCount() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return participantRepository.countAttendedEvents(currentUser.getUserId());
    }

    @Override
    public Users registerParticipant(ParticipantRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        Users user = new Users();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setActive(true);
        
        // Set participant role
        Roles participantRole = roleRepository.findByRoleName("PARTICIPANT")
                .orElseThrow(() -> new IllegalStateException("PARTICIPANT role not found"));
        user.getRoles().add(participantRole);
        
        Users savedUser = userRepository.save(user);
        
        // Create user details
        UserDetails userDetails = new UserDetails();
        userDetails.setUser(savedUser);
        userDetails.setFullName(registrationDto.getFullName());
        userDetails.setPhone(registrationDto.getPhone());
        userDetails.setDepartment(registrationDto.getDepartment());
        userDetails.setEnrollmentNo(registrationDto.getStudentId());
        userDetailsRepository.save(userDetails);
        
        return savedUser;
    }

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
