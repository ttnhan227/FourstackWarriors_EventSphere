package fpt.aptech.eventsphere.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final String APPLICATION_NAME = "EventSphere";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void sendEmail(String to, String subject, String body) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", principalName);
        if (client == null) {
            throw new RuntimeException("No authorized client found for user: " + principalName);
        }

        OAuth2AccessToken oauth2AccessToken = client.getAccessToken();
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        // Create a credential with the access token
        Credential credential = new GoogleCredential()
            .setAccessToken(oauth2AccessToken.getTokenValue());

        // Create Gmail service
        Gmail service = new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();

        // Create and send the email
        MimeMessage mimeMessage = createMimeMessage(to, subject, body);
        Message message = createMessageWithEmail(mimeMessage);

        service.users().messages().send("me", message).execute();
    }

    private MimeMessage createMimeMessage(String to, String subject, String body) throws MessagingException, java.io.UnsupportedEncodingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress("noreply@eventsphere.com", "EventSphere"));
            mimeMessage.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
            
            // Set content as HTML
            mimeMessage.setContent(body, "text/html; charset=utf-8");
        } catch (java.io.UnsupportedEncodingException e) {
            logger.error("Failed to encode email content", e);
            throw e;
        }

        return mimeMessage;
    }

    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
