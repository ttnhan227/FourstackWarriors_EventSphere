package fpt.aptech.eventsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDTO {
    int registrationId;
    int eventId;
    UserDTO user;
    LocalDateTime registeredOn;
    String status;
}
