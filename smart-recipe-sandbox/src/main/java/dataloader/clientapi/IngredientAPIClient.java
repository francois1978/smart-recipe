package dataloader.clientapi;

import dataloader.entity.Entity;
import dataloader.entity.IngredientEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class IngredientAPIClient extends AbstractAPIClient {


    public List<Entity> findAll() {
        //read all
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<IngredientEntity[]> response = restTemplate.getForEntity(
                SERVICE_URL + "ingredients", IngredientEntity[].class);

        List result = Arrays.asList(response.getBody());
        log.info("Number of total ingredients: " + result.size());
        return result;


    }

    public List findByName(String name) {
        log.info("Get ingredient by name: " + name);
        RestTemplate restTemplate = new RestTemplate();
        List ingredients = restTemplate.getForObject(SERVICE_URL + "ingredient/" + name, List.class);
        log.info("Number of total ingredient: " + ingredients.size());
        return ingredients;
    }


    public Entity create(Entity ingredientEntity) {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        ingredientEntity = restTemplate.postForObject(SERVICE_URL + "ingredient", ingredientEntity, IngredientEntity.class);
        log.info("Ingredient created: " + ingredientEntity.toString());
        return ingredientEntity;
    }

    @Override
    public Entity buildNewEntity(String name) {
        IngredientEntity entity = new IngredientEntity();
        entity.setName(name);
        return entity;
    }


}
