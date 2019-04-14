package smartrecipe.webgui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@PropertySource("classpath:/application.properties")
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class RecipeAPIClient {

    private static final Logger log = LoggerFactory.getLogger(RecipeAPIClient.class);

    @Value("${service.url}")
    private String serviceUrl;


    public void resetIngredientList(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForEntity(serviceUrl + "resetingredientlist/", Object.class);
        log.info("Recipe ingredient shopping lis done");
    }

    public List<String> addIngredientToShoppingList(Long recipeId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String[]> response = restTemplate.getForEntity(serviceUrl + "addingredientbyrecipe/" + recipeId, String[].class);
        List ingredients = Arrays.asList(response.getBody());
        log.info("Number of total ingredients: " + ingredients.size());
        return ingredients;

    }

    public List<RecipeEntity> findByKeyWord(String description) {
        //read all
        log.info("Get recipes by description: " + description);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RecipeEntity[]> response = restTemplate.getForEntity(serviceUrl + "recipesbyautodescription/" + description, RecipeEntity[].class);
        List recipes = Arrays.asList(response.getBody());
        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }

    public List<RecipeLight> findByKeyWordFullTextSearch(String description, Set<TagEntity> tagEntityList) {
        //read all
        log.info("Get recipes by description (full text search): " + description);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RecipeLight[]> response = null;
        List<RecipeLight> recipes = null;
        if (CollectionUtils.isEmpty(tagEntityList)) {
            response = restTemplate.getForEntity(serviceUrl + "recipesbyautodescriptionfull/" + description, RecipeLight[].class);
            recipes = Arrays.asList(response.getBody());
        } else {
            RecipeFindParameter parameter = new RecipeFindParameter();
            parameter.setDescription(description);
            parameter.setTags(tagEntityList);
            RecipeLight[] resultAsArray = restTemplate.postForObject(serviceUrl + "recipesbyautodescriptionfull/", parameter, RecipeLight[].class);
            recipes = Arrays.asList(resultAsArray);
        }

        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }

    void deleteById(Long id) {
        //read all
        log.info("Delete id: " + id);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(serviceUrl + "recipes/" + id);
    }

    public List<RecipeEntity> findAllRecipes() {
        //read all
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RecipeEntity[]> response = restTemplate.getForEntity(
                serviceUrl + "recipes", RecipeEntity[].class);

        List recipes = Arrays.asList(response.getBody());
        return recipes;

    }

    public RecipeEntity saveRecipeOcr(RecipeEntity recipe) {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        recipe = restTemplate.postForObject(serviceUrl + "recipesocr", recipe, RecipeEntity.class);
        log.info("Recipe created: " + recipe.toString());
        return recipe;
    }

    public RecipeEntity saveRecipeSimple(RecipeEntity recipe) {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        if (recipe.getId() != null) {
            RecipeEntity previousRecipeEntity = findRecipeById((recipe.getId()));
            if (!previousRecipeEntity.getName().equals(recipe.getName())) {
                recipe.setNameModifiedManual(true);
            }
        }
        recipe = restTemplate.postForObject(serviceUrl + "recipes", recipe, RecipeEntity.class);
        log.info("Recipe created: " + recipe.toString());
        return recipe;
    }

    public RecipeEntity findRecipeById(Long recipeId) {
        //read one
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = restTemplate.getForObject(serviceUrl + "recipes/" + recipeId, RecipeEntity.class);
        log.info("Recipe loaded from get by id: " + recipe.toString());
        return recipe;

    }


}
