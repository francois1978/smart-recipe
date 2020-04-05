package smartrecipe.service.sensor.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.sensor.dto.TempSensorDto;
import smartrecipe.service.sensor.entity.SensorDataEntity;
import smartrecipe.service.sensor.service.SensorService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class SensorController {

    @Resource
    SensorService sensorService;

    @RequestMapping(value = "/sensordata/save", method = RequestMethod.POST)
    @ApiOperation("Post temperature and humidity from sensor.")
    void saveTempSensorData(@RequestBody TempSensorDto tempSensorDto) {
        log.info("Sensor data received: " + tempSensorDto);
        sensorService.saveSensorData(tempSensorDto);
    }

    @GetMapping("/sensordata/getdata/{type}")
    @ApiOperation("Find sensor data by type")
    List<SensorDataEntity> findSensorDataByType(@PathVariable("type") String type) {
        List<SensorDataEntity> entityList = sensorService.findSensorDataByType(type);
        log.info("Number of data loaded: " + entityList.size());
        return entityList;
    }

    @GetMapping("/sensordata/getdata/{type}/{startdate}/{enddate}")
    @ApiOperation("Find sensor data by type")
    List<SensorDataEntity> findSensorDataByTypeAndDates(@PathVariable("type") String type,
                                                        @PathVariable("startdate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                        @PathVariable("enddate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("Get sensor data of type " + type + " between dates " + startDate + " / " + endDate);
        List<SensorDataEntity> entityList = sensorService.findSensorDataByTypeAndDates(type, startDate, endDate);
        log.info("Number of data loaded: " + entityList.size());
        return entityList;
    }


}
