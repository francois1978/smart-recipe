package dataloader.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.Date;

@ToString
@lombok.Getter
@lombok.Setter
@Builder
@AllArgsConstructor
public class SensorDataDto {

    private Long id;

    private String sensorName;

    private String type;

    private String valueText;

    private Double valueNumeric;

    private Date inputDate;

    public SensorDataDto() {
    }
}
