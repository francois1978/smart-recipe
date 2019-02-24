package smartrecipe.webgui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class RecipeAPIClient {

    private static final Logger log = LoggerFactory.getLogger(RecipeAPIClient.class);

    public List<RecipeEntity>  findByKeyWord(String description) {
        //read all
        log.info("Get recipes by description: " + description);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RecipeEntity[]> response = restTemplate.getForEntity("http://localhost:8080/sr/recipesbydescription/" + description, RecipeEntity[].class);
        List recipes = Arrays.asList(response.getBody());
        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }



    public List<RecipeEntity> findAllRecipes() {
        //read all
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RecipeEntity[]> response = restTemplate.getForEntity(
                "http://localhost:8080/sr/recipes", RecipeEntity[].class);

        List recipes = Arrays.asList(response.getBody());
        return recipes;

    }

    public RecipeEntity saveRecipe(RecipeEntity recipe) {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        recipe = restTemplate.postForObject("http://localhost:8080/sr/recipes", recipe, RecipeEntity.class);
        log.info("Simple recipe created: " + recipe.toString());
        return recipe;
    }

    public RecipeEntity findRecipeById(Long recipeId) {
        //read one
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = restTemplate.getForObject("http://localhost:8080/sr/recipes/" + recipeId, RecipeEntity.class);
        log.info("Recipe loaded from get by id: " + recipe.toString());
        return recipe;

    }
}
