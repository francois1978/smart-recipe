package smartrecipe.service.sensor.dto;

import lombok.AllArgsConstructor;

@lombok.Getter
@AllArgsConstructor
public enum SensorDataTypeEnum {

    TEMPERATURE("TEMPERATURE"),
    HUMIDITY("HUMIDITY");

    private String value;

}
