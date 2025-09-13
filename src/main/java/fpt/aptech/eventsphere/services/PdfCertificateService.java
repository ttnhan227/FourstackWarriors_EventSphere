package fpt.aptech.eventsphere.services;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import fpt.aptech.eventsphere.models.Certificates;
import fpt.aptech.eventsphere.models.Events;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PdfCertificateService {

    private static final BaseColor DARK_BLUE = new BaseColor(13, 71, 161);
    private static final BaseColor LIGHT_BLUE = new BaseColor(227, 242, 253);
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, DARK_BLUE);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.NORMAL);
    private static final Font NAME_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, DARK_BLUE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font ITALIC_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

    public byte[] generateCertificatePdf(Users user, Events event, Certificates certificate) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Add background
        addBackground(document, writer);
        
        // Add header
        addHeader(document);
        
        // Add content
        addContent(document, user, event, certificate);
        
        // Add footer
        addFooter(document, event);
        
        document.close();
        return baos.toByteArray();
    }
    
    private void addBackground(Document document, PdfWriter writer) {
        try {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Rectangle rect = document.getPageSize();
            
            // Create a background with light blue color and rounded corners
            canvas.setColorFill(new BaseColor(227, 242, 253, 25)); // 25 alpha for transparency
            canvas.roundRectangle(
                rect.getLeft() + 20, 
                rect.getBottom() + 20, 
                rect.getWidth() - 40, 
                rect.getHeight() - 40, 
                20
            );
            canvas.fill();
        } catch (Exception e) {
            // Background is not critical, continue without it
        }
    }
    
    private void addHeader(Document document) throws DocumentException {
        // Add logo if available
        try {
            URL logoUrl = new ClassPathResource("static/images/logo.png").getURL();
            Image logo = Image.getInstance(logoUrl);
            logo.scaleToFit(150, 150);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
            document.add(Chunk.NEWLINE);
        } catch (Exception e) {
            // Logo not found, continue without it
        }
        
        // Add title
        Paragraph title = new Paragraph("CERTIFICATE OF PARTICIPATION", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add decorative line
        Paragraph line = new Paragraph();
        line.add(Chunk.NEWLINE);
        line.add(new Chunk(new LineSeparator(1, 100, BaseColor.BLACK, Element.ALIGN_CENTER, 0)));
        line.setSpacingAfter(20);
        document.add(line);
    }
    
    private void addContent(Document document, Users user, Events event, Certificates certificate) throws DocumentException {
        // Add "This is to certify that"
        Paragraph p1 = new Paragraph("This is to certify that\n\n", HEADER_FONT);
        p1.setAlignment(Element.ALIGN_CENTER);
        document.add(p1);
        
        // Add participant name
        Paragraph name = new Paragraph(user.getEmail(), NAME_FONT);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(20);
        document.add(name);
        
        // Add event details
        Paragraph p2 = new Paragraph("has successfully participated in\n\n", HEADER_FONT);
        p2.setAlignment(Element.ALIGN_CENTER);
        document.add(p2);
        
        // Add event name
        Paragraph eventName = new Paragraph(event.getTitle(), NAME_FONT);
        eventName.setAlignment(Element.ALIGN_CENTER);
        eventName.setSpacingAfter(20);
        document.add(eventName);
        
        // Add event date
        String eventDate = "held on " + event.getStartDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "\n\n";
        Paragraph p3 = new Paragraph(eventDate, NORMAL_FONT);
        p3.setAlignment(Element.ALIGN_CENTER);
        document.add(p3);
        
        // Add certificate ID or temporary ID if certificate is null
        String certIdText = certificate != null ? 
            "Certificate ID: " + certificate.getCertificateId() : 
            "Temporary Certificate ID: " + UUID.randomUUID().toString().substring(0, 8);
        Paragraph certId = new Paragraph(certIdText, ITALIC_FONT);
        certId.setAlignment(Element.ALIGN_CENTER);
        document.add(certId);
    }
    
    private void addFooter(Document document, Events event) throws DocumentException {
        // Add some space
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        
        // Add signature line
        Paragraph signatureLine = new Paragraph();
        signatureLine.add(Chunk.NEWLINE);
        signatureLine.add(new Chunk(new LineSeparator(1, 100, BaseColor.BLACK, Element.ALIGN_CENTER, 0)));
        signatureLine.setAlignment(Element.ALIGN_CENTER);
        document.add(signatureLine);
        
        // Add signer name
        String signerText = "Event Organizer\n" + event.getOrganizer().getEmail();
        Paragraph signer = new Paragraph(signerText, NORMAL_FONT);
        signer.setAlignment(Element.ALIGN_CENTER);
        signer.setSpacingBefore(5);
        document.add(signer);
        
        // Add date
        String dateText = "Date: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        Paragraph date = new Paragraph(dateText, ITALIC_FONT);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingBefore(30);
        document.add(date);
    }
    
    // Simple line separator implementation using iText's built-in LineSeparator
    private static class LineSeparator extends com.itextpdf.text.pdf.draw.LineSeparator {
        public LineSeparator(float lineWidth, float percentage, BaseColor color, int align, float offset) {
            super(lineWidth, percentage, color, align, offset);
        }
    }
}
