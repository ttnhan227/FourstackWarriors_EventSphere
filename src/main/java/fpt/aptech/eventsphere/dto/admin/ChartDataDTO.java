package fpt.aptech.eventsphere.dto.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO {
    private String label;
    private BigDecimal value;
    private String color;
    private String period; // time-series
}