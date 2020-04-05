package smartrecipe.service.sensor.dto;

import lombok.AllArgsConstructor;

@lombok.Getter
@AllArgsConstructor
public enum SensorNameEnum {

    TEMPERATURE_HUMIDITY("TEMPERATURE_HUMIDITY");

    private String value;

}
