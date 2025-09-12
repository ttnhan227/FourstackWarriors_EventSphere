package fpt.aptech.eventsphere.dto.admin;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO {
    private String label;
    private Number value;
    private String color;

    public ChartDataDTO(String label, Number value) {
        this.label = label;
        this.value = value;
    }
}