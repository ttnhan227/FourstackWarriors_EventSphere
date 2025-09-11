package fpt.aptech.eventsphere.exceptions;

public class ProfilePictureUploadException extends RuntimeException {
    public ProfilePictureUploadException(String message) {
        super(message);
    }

    public ProfilePictureUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
