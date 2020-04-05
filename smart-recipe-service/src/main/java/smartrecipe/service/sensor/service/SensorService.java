package smartrecipe.service.sensor.service;

import org.springframework.stereotype.Service;
import smartrecipe.service.sensor.dto.TempSensorDto;
import smartrecipe.service.sensor.entity.SensorDataEntity;

import java.util.Date;
import java.util.List;

@Service
public interface SensorService {
    List<SensorDataEntity> findSensorDataByTypeAndDates(String type, Date startDate, Date endDate);

    List<SensorDataEntity> findSensorDataByType(String type);

    void saveSensorData(TempSensorDto tempSensorDto);
}
