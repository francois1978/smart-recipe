import dataloader.clientapi.APIClient;
import dataloader.dto.SensorDataDto;
import dataloader.dto.TempSensorDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
public class TestSensorService extends APIClient {


    public static void main(String args[]) throws IOException, ParseException {


        //createSensaorData();
        loadSensorDataBetweenDates();

    }

    private static void createSensaorData() {
        log.info("Creating sensor entity");
        TempSensorDto tempSensorDto = new TempSensorDto();
        tempSensorDto.setTemperature(22);
        tempSensorDto.setHumidity(45);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(SERVICE_URL + "sensordata/save", tempSensorDto, Object.class);
    }

    public static void loadSensorDataBetweenDates() {

        log.info("Loading sensor data");

        RestTemplate restTemplate = new RestTemplate();
        SensorDataDto[] sensorDataDtos =
                restTemplate.getForObject(SERVICE_URL + "/sensordata/getdata/TEMPERATURE/" +
                                "2020-03-14" + "/" + "2020-03-14",
                        SensorDataDto[].class);

        log.info("Sensor data list size " + sensorDataDtos.length);

    }


}
