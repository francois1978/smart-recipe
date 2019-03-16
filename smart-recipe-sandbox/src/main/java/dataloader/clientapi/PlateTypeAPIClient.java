package dataloader.clientapi;

import dataloader.entity.Entity;
import dataloader.entity.PlateTypeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class PlateTypeAPIClient extends AbstractAPIClient {


    public List<Entity> findAll() {
        //read all
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<PlateTypeEntity[]> response = restTemplate.getForEntity(
                SERVICE_URL + "platetype", PlateTypeEntity[].class);

        List result = Arrays.asList(response.getBody());
        log.info("Number of total elements: " + result.size());
        return result;


    }

    public List findByName(String name) {
        log.info("Get plat type by name: " + name);
        RestTemplate restTemplate = new RestTemplate();
        List plateTypes = restTemplate.getForObject(SERVICE_URL + "platetype/" + name, List.class);
        log.info("Number of total type: " + plateTypes.size());
        return plateTypes;
    }


    public Entity create(Entity plateTypeEntity) {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        plateTypeEntity = restTemplate.postForObject(SERVICE_URL + "platetype", plateTypeEntity, PlateTypeEntity.class);
        log.info("Plate type created: " + plateTypeEntity.toString());
        return plateTypeEntity;
    }

    @Override
    public Entity buildNewEntity(String name) {
        PlateTypeEntity entity = new PlateTypeEntity();
        entity.setName(name);
        return entity;
    }


}
