package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.CertificateDTO;
import fpt.aptech.eventsphere.models.Certificates;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.CertificateRepository;
import fpt.aptech.eventsphere.repositories.EventRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.itextpdf.text.DocumentException;

@Service
@Transactional
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PdfCertificateService pdfCertificateService;
    private final Path fileStorageLocation = Paths.get("certificates");

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                UserRepository userRepository,
                                EventRepository eventRepository,
                                PdfCertificateService pdfCertificateService) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.pdfCertificateService = pdfCertificateService;
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for certificates", ex);
        }
    }

    @Override
    public List<CertificateDTO> getUserCertificates() {
        Integer userId = getCurrentUserId();
        return certificateRepository.findWithEventDetailsByStudentId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CertificateDTO> getDownloadableCertificates() {
        Integer userId = getCurrentUserId();
        return certificateRepository.findDownloadableCertificates(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<Resource> downloadCertificate(Integer certificateId) throws IOException {
        Integer userId = getCurrentUserId();
        Optional<Certificates> certificateOpt = certificateRepository.findById(certificateId);
        
        if (certificateOpt.isEmpty() || certificateOpt.get().getStudent().getUserId() != userId) {
            throw new RuntimeException("Certificate not found or access denied");
        }

        Certificates certificate = certificateOpt.get();
        if (certificate.getCertificateUrl() == null) {
            throw new RuntimeException("Certificate URL is not available");
        }

        Path filePath = this.fileStorageLocation.resolve(certificate.getCertificateUrl()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("Certificate file not found");
        }

        String contentType = "application/octet-stream";
        String fileName = String.format("certificate_%s_%s.pdf", 
            certificate.getEvent().getTitle().replaceAll("\\s+", "_"),
            certificate.getStudent().getUserId() == userId ? certificate.getStudent().getEmail() : "participant");
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
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
    public CertificateDTO generateCertificate(Integer userId, Integer eventId) throws IOException, DocumentException {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Events event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
            
        // Check if certificate already exists
        Optional<Certificates> existingCert = certificateRepository.findByStudent_UserIdAndEvent_EventId(userId, eventId);
        if (existingCert.isPresent() && existingCert.get().getCertificateUrl() != null) {
            return convertToDto(existingCert.get());
        }
        
        // Generate PDF
        byte[] pdfBytes = pdfCertificateService.generateCertificatePdf(user, event, existingCert.orElse(null));
        
        // Save PDF to file
        String fileName = String.format("cert_%s_%s_%s.pdf", 
            user.getEmail(), // Using email as username, 
            event.getEventId(), 
            UUID.randomUUID().toString().substring(0, 8));
            
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        try (InputStream inputStream = new ByteArrayInputStream(pdfBytes)) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Create or update certificate record
        Certificates certificate = existingCert.orElseGet(() -> {
            Certificates newCert = new Certificates();
            newCert.setStudent(user);
            newCert.setEvent(event);
            newCert.setIssuedOn(LocalDateTime.now());
            return newCert;
        });
        
        certificate.setCertificateUrl(fileName);
        certificate = certificateRepository.save(certificate);
        
        return convertToDto(certificate);
    }
    
    @Override
    public boolean canGenerateCertificate(Integer userId, Integer eventId) {
        // Check if user is registered for the event
        boolean isRegistered = eventRepository.findById(eventId)
            .map(e -> e.getRegistrations().stream()
                .anyMatch(r -> r.getStudent().getUserId() == userId))
            .orElse(false);
            
        // Check if event is in the past
        boolean isEventPast = eventRepository.findById(eventId)
            .map(e -> e.getEndDate().isBefore(LocalDateTime.now()))
            .orElse(false);
            
        // Check if certificate already exists
        boolean certificateExists = certificateRepository
            .findByStudent_UserIdAndEvent_EventId(userId, eventId)
            .isPresent();
        
        return isRegistered && isEventPast && !certificateExists;
    }
    
    @Override
    public Double getCertificateFee(Integer eventId) {
        // Return a default fee since there's no certificate fee field in Events
        // In a real application, this would come from event settings or configuration
        return 0.0;
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
