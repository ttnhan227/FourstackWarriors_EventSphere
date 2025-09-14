package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.CertificateDTO;
import fpt.aptech.eventsphere.models.Certificates;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.CertificateRepository;
import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.itextpdf.text.*;

@Service
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final Path fileStorageLocation;
    private final PdfCertificateService pdfCertificateService;
    

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                UserRepository userRepository,
                                EventRepository eventRepository,
                                PdfCertificateService pdfCertificateService) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.pdfCertificateService = pdfCertificateService;
        
        try {
            // Store certificates in the project's certificates directory
            this.fileStorageLocation = Paths.get("certificates").toAbsolutePath();
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("Certificate storage location: " + this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for certificates", ex);
        }
    }

    @Override
    public List<CertificateDTO> getUserCertificates(Integer userId) {
        return certificateRepository.findWithEventDetailsByStudentId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CertificateDTO> getAvailableCertificates(Integer userId) {
        // Get events that the user has attended but doesn't have certificates for
        List<Events> attendedEvents = eventRepository.findAttendedEventsByUserId(userId);
        
        return attendedEvents.stream()
            .map(event -> {
                CertificateDTO dto = new CertificateDTO();
                dto.setEventId(event.getEventId());
                dto.setEventName(event.getTitle());
                dto.setFeeAmount(event.getCertificateFee() != null ? event.getCertificateFee() : 0.0);
                dto.setPaid(false); // Initially not paid
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<CertificateDTO> findByUserIdAndEventId(Integer userId, Integer eventId) {
        return certificateRepository.findByStudentIdAndEventId(userId, eventId)
                .map(this::convertToDto);
    }

    @Override
    public ResponseEntity<Resource> downloadCertificate(Integer certificateId) throws IOException {
        Integer userId = getCurrentUserId();
        Optional<Certificates> certificateOpt = certificateRepository.findById(certificateId);

        if (certificateOpt.isEmpty()) {
            throw new RuntimeException("Certificate not found");
        }

        Certificates certificate = certificateOpt.get();
        Users user = certificate.getStudent();
        Events event = certificate.getEvent();

        if (user == null || user.getUserId() != userId) {
            throw new RuntimeException("Access denied");
        }

        if (!certificate.isPaid()) {
            throw new RuntimeException("Certificate fee has not been paid.");
        }

        try {
            byte[] pdfBytes = pdfCertificateService.generateCertificatePdf(user, event, certificate);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            String fileName = String.format("certificate_%s_%s.pdf",
                    event.getTitle().replaceAll("\\s+", "_"),
                    user.getEmail());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (DocumentException e) {
            throw new IOException("Error generating PDF certificate", e);
        }
    }

    @Override
    public boolean isCertificateAvailable(Integer certificateId) {
        try {
            Integer userId = getCurrentUserId();
            return certificateRepository.findByCertificateIdAndStudent_UserId(certificateId, userId).isPresent();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    @Transactional
    public CertificateDTO generateCertificate(Integer userId, Integer eventId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Certificates certificate = new Certificates();
        certificate.setStudent(user);
        certificate.setEvent(event);
        certificate.setIssuedOn(LocalDateTime.now());
        certificate.setPaid(event.getCertificateFee() == null || event.getCertificateFee() <= 0);
        certificate.setDownloadCount(0);
        certificate.setCertificateUrl("generated-on-download"); // No longer storing files

        Certificates savedCertificate = certificateRepository.save(certificate);
        return convertToDto(savedCertificate);
    }
    
    @Override
    public boolean canGenerateCertificate(Integer userId, Integer eventId) {
        // Check if user is registered for the event
        if (!eventRepository.existsByEventIdAndUserId(eventId, userId)) {
            return false;
        }
        
        // Check if event has ended
        boolean isEventPast = eventRepository.findById(eventId)
            .map(e -> e.getEndDate().isBefore(LocalDateTime.now()))
            .orElse(false);
            
        if (!isEventPast) {
            return false;
        }
        
        // Check if certificate already exists
        return !certificateRepository.existsByStudent_UserIdAndEvent_EventId(userId, eventId);
    }
    
    @Override
    public Double getCertificateFee(Integer eventId) {
        return eventRepository.findById(eventId)
                .map(Events::getCertificateFee)
                .orElse(0.0);
    }
    
    @Override
    @Transactional
    public void markCertificateAsPaid(Integer userId, Integer certificateId) {
        Certificates certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new RuntimeException("Certificate not found"));
            
        // Verify the certificate belongs to the user
        if (certificate.getStudent().getUserId() != userId.intValue()) {
            throw new RuntimeException("Certificate does not belong to the specified user");
        }
        
        certificate.setPaid(true);
        certificateRepository.save(certificate);
    }
    
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
    }

    private CertificateDTO convertToDto(Certificates certificate) {
        CertificateDTO dto = new CertificateDTO();
        dto.setCertificateId(certificate.getCertificateId());
        dto.setEventId(certificate.getEvent().getEventId());
        dto.setEventName(certificate.getEvent().getTitle()); // Using getTitle() instead of getEventName()
        dto.setIssuedOn(certificate.getIssuedOn());
        dto.setCertificateUrl(certificate.getCertificateUrl());
        
        // Default values - these would be set based on your business logic
        dto.setFeeAmount(0.0);
        dto.setPaid(true);
        
        return dto;
    }
}
