package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Certificates;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.models.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PdfCertificateServiceTest {

    @Autowired
    private PdfCertificateService pdfCertificateService;

    private Users testUser;
    private Events testEvent;
    private Certificates testCertificate;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new Users();
        testUser.setUserId(1);
        testUser.setEmail("test@example.com");
        UserDetails userDetails = new UserDetails();
        userDetails.setFullName("Test User");
        testUser.setUserDetails(userDetails);

        // Create a test event
        testEvent = new Events();
        testEvent.setEventId(1);
        testEvent.setTitle("Test Event");
        testEvent.setStartDate(LocalDateTime.now().minusDays(1));
        testEvent.setEndDate(LocalDateTime.now().plusDays(1));
        
        // Set organizer
        Users organizer = new Users();
        organizer.setUserId(2);
        organizer.setEmail("organizer@example.com");
        UserDetails organizerDetails = new UserDetails();
        organizerDetails.setFullName("Event Organizer");
        organizer.setUserDetails(organizerDetails);
        testEvent.setOrganizer(organizer);

        // Create a test certificate
        testCertificate = new Certificates();
        testCertificate.setCertificateId(1);
        testCertificate.setIssuedOn(LocalDateTime.now());
    }

    @Test
    void testGenerateCertificatePdf() throws Exception {
        // Generate the PDF
        byte[] pdfBytes = pdfCertificateService.generateCertificatePdf(testUser, testEvent, testCertificate);
        
        // Verify the PDF was generated
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Optional: Save the PDF for manual inspection
        try (FileOutputStream fos = new FileOutputStream("test-certificate.pdf")) {
            fos.write(pdfBytes);
        }
    }

    @Test
    void testGenerateCertificatePdfWithNullCertificate() throws Exception {
        // Test with null certificate (should create a new one)
        byte[] pdfBytes = pdfCertificateService.generateCertificatePdf(testUser, testEvent, null);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
