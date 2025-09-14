package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.CertificateDTO;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.services.CertificateService;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/certificates")
@PreAuthorize("isAuthenticated()")
public class CertificateController {

    private final CertificateService certificateService;
    private final UserRepository userRepository;

    public CertificateController(CertificateService certificateService, UserRepository userRepository) {
        this.certificateService = certificateService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String getUserCertificates(Model model) {
        Users currentUser = getCurrentUser();
        List<CertificateDTO> certificates = certificateService.getUserCertificates(currentUser.getUserId());
        List<CertificateDTO> availableCerts = certificateService.getAvailableCertificates(currentUser.getUserId());

        model.addAttribute("certificates", certificates);
        model.addAttribute("availableCertificates", availableCerts);

        // Calculate total unpaid fees
        double totalUnpaidFees = certificates.stream()
                .filter(cert -> !cert.isPaid() && cert.getFeeAmount() > 0)
                .mapToDouble(CertificateDTO::getFeeAmount)
                .sum();
        model.addAttribute("totalUnpaidFees", totalUnpaidFees);

        return "certificates/list";
    }

    @GetMapping("/available")
    public String getAvailableCertificates(Model model) {
        Users currentUser = getCurrentUser();
        List<CertificateDTO> certificates = certificateService.getAvailableCertificates(currentUser.getUserId());
        model.addAttribute("availableCertificates", certificates);

        // Calculate total unpaid fees
        double totalFees = certificates.stream()
                .filter(cert -> cert.getFeeAmount() > 0)
                .mapToDouble(CertificateDTO::getFeeAmount)
                .sum();

        model.addAttribute("totalFees", totalFees);
        return "certificates/available";
    }

    @GetMapping("/download/{certificateId}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable Integer certificateId) {
        try {
            System.out.println("Attempting to download certificate ID: " + certificateId);
            if (!certificateService.isCertificateAvailable(certificateId)) {
                System.out.println("Certificate not found or not available: " + certificateId);
                return ResponseEntity.notFound().build();
            }
            System.out.println("Certificate found, proceeding with download...");
            return certificateService.downloadCertificate(certificateId);
        } catch (Exception e) {
            System.out.println("Error downloading certificate " + certificateId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/generate/{eventId}")
    public String generateCertificate(@PathVariable Integer eventId, Model model, HttpServletRequest request) {
        try {
            // Get current user
            Users currentUser = getCurrentUser();
            System.out.println("Generating certificate for user: " + currentUser.getUserId() + ", event: " + eventId);

            // Check if certificate already exists
            Optional<CertificateDTO> existingCert = certificateService.findByUserIdAndEventId(
                currentUser.getUserId(), 
                eventId
            );
            
            if (existingCert.isPresent()) {
                CertificateDTO cert = existingCert.get();
                System.out.println("Certificate already exists: " + cert.getCertificateId());
                
                if (!cert.isPaid() && cert.getFeeAmount() > 0) {
                    model.addAttribute("certificate", cert);
                    model.addAttribute("contextPath", request.getContextPath());
                    return "certificates/payment";
                }
                
                // If certificate is already paid, redirect to certificates list
                return "redirect:/certificates?error=Certificate already exists";
            }

            try {
                // Generate new certificate
                System.out.println("Generating new certificate...");
                CertificateDTO certificate = certificateService.generateCertificate(
                    currentUser.getUserId(), 
                    eventId
                );
                System.out.println("Certificate generated with ID: " + certificate.getCertificateId());

                if (!certificate.isPaid() && certificate.getFeeAmount() > 0) {
                    // Redirect to payment page if certificate has a fee
                    model.addAttribute("certificate", certificate);
                    model.addAttribute("contextPath", request.getContextPath());
                    return "certificates/payment";
                }

                return "redirect:/certificates?success=Certificate generated successfully";
                
            } catch (Exception e) {
                System.err.println("Error generating certificate: " + e.getMessage());
                e.printStackTrace();
                return "redirect:/certificates?error=Failed to generate certificate: " + 
                       URLEncoder.encode(e.getMessage(), "UTF-8");
            }
            
        } catch (Exception e) {
            System.err.println("Unexpected error in generateCertificate: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/certificates?error=An unexpected error occurred";
        }
    }

    @PostMapping("/{certificateId}/pay")
    public String processPayment(@PathVariable Integer certificateId) {
        try {
            certificateService.markCertificateAsPaid(getCurrentUser().getUserId(), certificateId);
            return "redirect:/certificates?payment=success";
        } catch (Exception e) {
            return "redirect:/certificates?paymentError=" + e.getMessage();
        }
    }

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));
        }
        throw new SecurityException("User not authenticated");
    }

    @GetMapping("/can-generate/{eventId}")
    public String canGenerateCertificate(
            @PathVariable Integer eventId,
            Model model) {
        model.addAttribute("canGenerate",
                certificateService.canGenerateCertificate(getCurrentUser().getUserId(), eventId));
        return "certificates/fragments :: canGenerate";
    }

    @GetMapping("/fee/{eventId}")
    @ResponseBody
    public Double getCertificateFee(@PathVariable Integer eventId) {
        return certificateService.getCertificateFee(eventId);
    }

    @GetMapping("/{certificateId}/availability")
    public ResponseEntity<?> checkCertificateAvailability(@PathVariable Integer certificateId) {
        try {
            boolean isAvailable = certificateService.isCertificateAvailable(certificateId);
            return ResponseEntity.ok().body("{\"available\": " + isAvailable + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
