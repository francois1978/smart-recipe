package dataloader.dto;


import lombok.ToString;

@lombok.Getter
@lombok.Setter
@ToString
public class TempSensorDto {

    private double temperature;
    private double humidity;

}
