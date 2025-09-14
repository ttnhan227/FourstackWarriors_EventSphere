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

@Service
public class PdfCertificateService {

    private static final BaseColor DARK_BLUE = new BaseColor(13, 71, 161);
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
        // Add recipient name
        Paragraph recipient = new Paragraph("This certificate is awarded to", HEADER_FONT);
        recipient.setAlignment(Element.ALIGN_CENTER);
        document.add(recipient);
        
        // Add user's name (using email as identifier)
        String userName = user.getEmail() != null ? user.getEmail() : "Participant";
        Paragraph name = new Paragraph(userName, NAME_FONT);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(15);
        document.add(name);
        
        // Add participation text
        Paragraph participation = new Paragraph("has successfully participated in", HEADER_FONT);
        participation.setAlignment(Element.ALIGN_CENTER);
        participation.setSpacingAfter(15);
        document.add(participation);
        
        // Add event title
        String title = event.getTitle() != null ? event.getTitle() : "the event";
        Paragraph eventTitle = new Paragraph("\"" + title + "\"", 
            new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, DARK_BLUE));
        eventTitle.setAlignment(Element.ALIGN_CENTER);
        eventTitle.setSpacingAfter(10);
        document.add(eventTitle);
        
        // Add event details
        Paragraph details = new Paragraph();
        details.setAlignment(Element.ALIGN_CENTER);
        
        // Add event date
        if (event.getStartDate() != null) {
            details.add(new Chunk("Held on: " + 
                event.getStartDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), 
                NORMAL_FONT));
            details.add(Chunk.NEWLINE);
        }
        
        // Add venue if available
        if (event.getVenue() != null && event.getVenue().getName() != null) {
            details.add(new Chunk("Venue: " + event.getVenue().getName(), NORMAL_FONT));
            details.add(Chunk.NEWLINE);
        }
        
        // Add organizer if available (using email as identifier)
        if (event.getOrganizer() != null) {
            String organizerName = event.getOrganizer().getEmail() != null ? 
                event.getOrganizer().getEmail() : "Organizer";
            details.add(new Chunk("Organized by: " + organizerName, NORMAL_FONT));
            details.add(Chunk.NEWLINE);
        }
        
        details.setSpacingAfter(30);
        document.add(details);
        
        // Add signatures
        addSignatures(document, event, certificate);
    }
    
    private void addSignatures(Document document, Events event, Certificates certificate) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(20);
        
        // Organizer signature
        PdfPCell organizerCell = new PdfPCell();
        organizerCell.setBorder(Rectangle.NO_BORDER);
        organizerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        // Add signature line
        Paragraph organizerLine = new Paragraph("________________________");
        organizerLine.setAlignment(Element.ALIGN_CENTER);
        organizerCell.addElement(organizerLine);
        
        // Add organizer name (using email as identifier)
        if (event.getOrganizer() != null) {
            String organizerName = event.getOrganizer().getEmail() != null ? 
                event.getOrganizer().getEmail() : "Organizer";
            Paragraph orgNamePara = new Paragraph(organizerName, NORMAL_FONT);
            orgNamePara.setAlignment(Element.ALIGN_CENTER);
            organizerCell.addElement(orgNamePara);
        }
        
        // Add title
        Paragraph organizerTitle = new Paragraph("Event Organizer", ITALIC_FONT);
        organizerTitle.setAlignment(Element.ALIGN_CENTER);
        organizerCell.addElement(organizerTitle);
        
        table.addCell(organizerCell);
        
        // Date
        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        // Add signature line
        Paragraph dateLine = new Paragraph("________________________");
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateCell.addElement(dateLine);
        
        // Add date (use current date if issuedOn is null)
        java.time.LocalDate issueDate = certificate != null && certificate.getIssuedOn() != null ? 
            certificate.getIssuedOn().toLocalDate() : java.time.LocalDate.now();
            
        Paragraph date = new Paragraph(
            issueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), 
            NORMAL_FONT);
        date.setAlignment(Element.ALIGN_CENTER);
        dateCell.addElement(date);
        
        // Add title
        Paragraph dateTitle = new Paragraph("Date", ITALIC_FONT);
        dateTitle.setAlignment(Element.ALIGN_CENTER);
        dateCell.addElement(dateTitle);
        
        table.addCell(dateCell);
        
        document.add(table);
    }
    
    private void addFooter(Document document, Events event) throws DocumentException {
        // Add some space
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        
        // Add certificate ID
        if (event.getEventId() != 0) {
            String title = event.getTitle() != null ? 
                event.getTitle().replaceAll("\\s+", "-").toLowerCase() : "event";
            String certId = String.format("Certificate ID: %s-%d", 
                title,
                event.getEventId());
                
            Paragraph idParagraph = new Paragraph(certId, 
                new Font(Font.FontFamily.COURIER, 8, Font.ITALIC, BaseColor.GRAY));
            idParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(idParagraph);
        }
        
        // Add terms and conditions
        Paragraph terms = new Paragraph(
            "This certificate is issued as a recognition of participation. " +
            "The authenticity of this certificate can be verified with the event organizers.",
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        terms.setAlignment(Element.ALIGN_CENTER);
        terms.setSpacingBefore(20);
        document.add(terms);
    }
    
    // Simple line separator implementation using iText's built-in LineSeparator
    private static class LineSeparator extends com.itextpdf.text.pdf.draw.LineSeparator {
        public LineSeparator(float lineWidth, float percentage, BaseColor color, int align, float offset) {
            super(lineWidth, percentage, color, align, offset);
        }
    }
}
