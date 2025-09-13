package fpt.aptech.eventsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Integer certificateId;
    private Integer eventId;
    private String eventName;
    private LocalDateTime issuedOn;
    private String certificateUrl;
    private boolean isPaid;  // To track if the certificate fee is paid
    private Double feeAmount; // The fee amount if applicable
}
