package fpt.aptech.eventsphere.services;

import java.util.List;

public interface EmailService {
    void sendEmail(String to, String subject, String body) throws Exception;
    public void sendEmailToUsers(List<String> recipients, String subject, String body);
    public void sendEmailWithAttachment(List<String> recipients, String subject, String body, byte[] attachment, String attachmentName);
}
