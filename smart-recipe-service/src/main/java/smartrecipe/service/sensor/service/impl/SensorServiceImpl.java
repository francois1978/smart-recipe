package smartrecipe.service.sensor.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smartrecipe.service.sensor.dto.SensorDataTypeEnum;
import smartrecipe.service.sensor.dto.SensorNameEnum;
import smartrecipe.service.sensor.dto.TempSensorDto;
import smartrecipe.service.sensor.entity.SensorDataEntity;
import smartrecipe.service.sensor.repository.SensorDataRepository;
import smartrecipe.service.sensor.service.SensorService;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
public class SensorServiceImpl implements SensorService {

    @Resource
    SensorDataRepository sensorDataRepository;


    @Override
    public List<SensorDataEntity> findSensorDataByTypeAndDates(String type, Date startDate, Date endDate) {

        //add one day to end date to have data for interval between dates including end date
        LocalDate endDatePlusOneDay = endDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate().plusDays(1);

        return sensorDataRepository.findAllByTypeAndInputDateBetween(type,
                startDate,
                java.util.Date.from(endDatePlusOneDay.atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()));
    }

    @Override
    public List<SensorDataEntity> findSensorDataByType(String type) {
        return sensorDataRepository.findByType(type);
    }


    @Override
    public void saveSensorData(TempSensorDto tempSensorDto) {

        SensorDataEntity sensorDataEntity = SensorDataEntity.builder().
                sensorName(SensorNameEnum.TEMPERATURE_HUMIDITY.getValue()).
                inputDate(new Date()).
                type(SensorDataTypeEnum.TEMPERATURE.getValue()).
                valueNumeric(tempSensorDto.getTemperature()).build();

        sensorDataEntity = sensorDataRepository.save(sensorDataEntity);

        log.info("Sensor data created " + sensorDataEntity);

        SensorDataEntity sensorDataEntityHumidity = SensorDataEntity.builder().
                sensorName(SensorNameEnum.TEMPERATURE_HUMIDITY.getValue()).
                inputDate(new Date()).
                type(SensorDataTypeEnum.HUMIDITY.getValue()).
                valueNumeric(tempSensorDto.getTemperature()).build();

        sensorDataEntityHumidity = sensorDataRepository.save(sensorDataEntityHumidity);

        log.info("Sensor data created " + sensorDataEntity);

    }

}
