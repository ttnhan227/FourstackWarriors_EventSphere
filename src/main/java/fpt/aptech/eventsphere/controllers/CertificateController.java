package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.services.CertificateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public String getUserCertificates(Model model) {
        model.addAttribute("certificates", certificateService.getUserCertificates());
        return "certificates/list";
    }

    @GetMapping("/available")
    public String getAvailableCertificates(Model model) {
        model.addAttribute("availableCertificates", certificateService.getDownloadableCertificates());
        return "certificates/available";
    }
    
    @GetMapping("/download/{certificateId}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable Integer certificateId) {
        try {
            if (!certificateService.isCertificateAvailable(certificateId)) {
                return ResponseEntity.notFound().build();
            }
            ResponseEntity<Resource> response = certificateService.downloadCertificate(certificateId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"certificate_" + certificateId + ".pdf\"")
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/generate/{eventId}")
    public String generateCertificate(@PathVariable Integer eventId) {
        try {
            certificateService.generateCertificate(
                getCurrentUserId(), 
                eventId
            );
            return "redirect:/certificates?success";
        } catch (Exception e) {
            return "redirect:/certificates?error=" + e.getMessage();
        }
    }
    
    private Integer getCurrentUserId() {
        // This is a placeholder - you'll need to implement this based on your authentication setup
        // For example, if using Spring Security with a custom UserDetails:
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            // Assuming your UserDetails has a method to get the user ID
            // You'll need to replace this with your actual implementation
            return 1; // Replace with actual user ID retrieval
        }
        throw new SecurityException("User not authenticated");
    }
    
    @GetMapping("/can-generate/{eventId}")
    public String canGenerateCertificate(
            @PathVariable Integer eventId,
            Model model) {
        model.addAttribute("canGenerate", 
            certificateService.canGenerateCertificate(getCurrentUserId(), eventId));
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
