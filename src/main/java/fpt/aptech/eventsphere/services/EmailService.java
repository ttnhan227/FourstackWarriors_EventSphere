package fpt.aptech.eventsphere.services;

import java.util.List;

public interface EmailService {
    public void sendEmailToUsers(List<String> recipients, String subject, String body);
    public void sendEmailWithAttachment(List<String> recipients, String subject, String body, byte[] attachment, String attachmentName);
}
