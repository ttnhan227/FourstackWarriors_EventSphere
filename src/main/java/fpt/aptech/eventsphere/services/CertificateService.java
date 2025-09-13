package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.CertificateDTO;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface CertificateService {
    
    /**
     * Get all certificates for the current user
     * @return List of CertificateDTO objects
     */
    List<CertificateDTO> getUserCertificates();
    
    /**
     * Get all downloadable certificates for the current user
     * @return List of downloadable CertificateDTO objects
     */
    List<CertificateDTO> getDownloadableCertificates();
    
    /**
     * Download a certificate file
     * @param certificateId ID of the certificate to download
     * @return ResponseEntity containing the certificate file
     * @throws IOException if there's an error reading the file
     */
    ResponseEntity<Resource> downloadCertificate(Integer certificateId) throws IOException;
    
    /**
     * Check if a certificate is available for download
     * @param certificateId ID of the certificate to check
     * @return true if the certificate is available for download
     */
    boolean isCertificateAvailable(Integer certificateId);
    
    /**
     * Generate a certificate for a participant
     * @param userId The ID of the participant
     * @param eventId The ID of the event
     * @return CertificateDTO of the generated certificate
     */
    CertificateDTO generateCertificate(Integer userId, Integer eventId) throws IOException, com.itextpdf.text.DocumentException;
    
    /**
     * Check if a certificate can be generated for a participant
     * @param userId The ID of the participant
     * @param eventId The ID of the event
     * @return true if a certificate can be generated
     */
    boolean canGenerateCertificate(Integer userId, Integer eventId);
    
    /**
     * Get the certificate fee for an event
     * @param eventId The ID of the event
     * @return The certificate fee amount
     */
    Double getCertificateFee(Integer eventId);
}
