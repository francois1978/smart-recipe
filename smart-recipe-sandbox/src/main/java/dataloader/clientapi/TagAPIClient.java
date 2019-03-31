package dataloader.clientapi;

import dataloader.entity.Entity;
import dataloader.entity.TagEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class TagAPIClient extends AbstractAPIClient {


    public List<Entity> findAll() {
        //read all
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<TagEntity[]> response = restTemplate.getForEntity(
                SERVICE_URL + "tags", TagEntity[].class);

        List result = Arrays.asList(response.getBody());
        log.info("Number of total tags: " + result.size());
        return result;

    }

    @Override
    public List findByName(String name) {
        return null;
    }


    public Entity create(Entity tagEntity) {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        tagEntity = restTemplate.postForObject(SERVICE_URL + "tag", tagEntity, TagEntity.class);
        log.info("Ingredient created: " + tagEntity.toString());
        return tagEntity;
    }

    @Override
    public Entity buildNewEntity(String name) {
        TagEntity entity = new TagEntity();
        entity.setName(name);
        return entity;
    }

    public void delete(Long id) {
        log.info("Delete id: " + id);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(SERVICE_URL + "tag/" + id);
    }


}
