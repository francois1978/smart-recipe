package smartrecipe.service.sensor.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sensor_data")
@ToString
@lombok.Getter
@lombok.Setter
@Builder
@AllArgsConstructor
public class SensorDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_name")
    private String sensorName;

    private String type;

    @Column(name = "value_text")
    private String valueText;

    @Column(name = "value_numeric")
    private Double valueNumeric;

    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "input_date")
    private Date inputDate;

    public SensorDataEntity() {
    }
}
