package dataloader.clientapi;

import dataloader.dto.RecipeFindParameter;
import dataloader.entity.RecipeBinaryEntity;
import dataloader.entity.RecipeEntity;
import dataloader.entity.TagEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import utils.Hash;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
public class RecipeAPIClient extends APIClient {


    private String recipePathIn = "/recipe1.jpg";
    private String recipePathOut = "recipe_" + System.currentTimeMillis() + ".jpg";

    public List<RecipeEntity> testFindAll() {
        //read all
        RestTemplate restTemplate = new RestTemplate();
        //List recipes = restTemplate.getForObject("http://localhost:8080/sr/recipes", List.class);
        //log.info("Number of total recipes: " + recipes.size());

        ResponseEntity<RecipeEntity[]> response = restTemplate.getForEntity(
                SERVICE_URL + "recipes", RecipeEntity[].class);

        List result = Arrays.asList(response.getBody());
        log.info("Number of total recipes: " + result.size());
        return result;


    }

    public List<Integer> findAllRecipeIds() {
        //read all ids
        RestTemplate restTemplate = new RestTemplate();
        List<Integer> allIds = restTemplate.getForObject(SERVICE_URL + "recipesids/", List.class);

        log.info("Number of total ids: " + allIds.size());
        return allIds;


    }

    public void rebuildLuceneIndexes() {
        log.info("Rebuild lucene indexe...");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(SERVICE_URL + "buildluceneindex/", Object.class);

    }


    public List<RecipeBinaryEntity> findByChecksum(String checksum) {
        RestTemplate restTemplate = new RestTemplate();
        List<RecipeBinaryEntity> recipeBinaryEntity = restTemplate.getForObject(SERVICE_URL + "recipesbychecksum/" + checksum, List.class);
        return recipeBinaryEntity;

    }

    public List testByDescription(String description) {
        //read all
        log.info("Get recipes by description: " + description);
        RestTemplate restTemplate = new RestTemplate();
        List recipes = restTemplate.getForObject(SERVICE_URL + "recipesbydescription/" + description, List.class);
        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }

    public List testByAutoDescription(String description) {
        //read all
        log.info("Get recipes by auto description: " + description);
        RestTemplate restTemplate = new RestTemplate();
        List recipes = restTemplate.getForObject(SERVICE_URL + "recipesbyautodescription/" + description, List.class);
        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }

    public List testByAutoDescriptionFull(String description) {
        //read all
        log.info("Get recipes by auto description full text search: " + description);
        RestTemplate restTemplate = new RestTemplate();
        List recipes = restTemplate.getForObject(SERVICE_URL + "recipesbyautodescriptionfull/" + description, List.class);
        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }

    public List findByAutoDescriptionFullAndTags(String description, Set<TagEntity> tags) {
        //read all
        log.info("Get recipes by tags and auto description full text search: " + description);
        RecipeFindParameter parameter = new RecipeFindParameter();
        parameter.setDescription(description);
        parameter.setTags(tags);
        RestTemplate restTemplate = new RestTemplate();
        List recipes = restTemplate.postForObject(SERVICE_URL + "recipesbyautodescriptionfull/", parameter, List.class);
        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }

    public RecipeEntity testFindOne(Long recipeId) {
        //read one
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = restTemplate.getForObject(SERVICE_URL + "recipes/" + recipeId, RecipeEntity.class);
        log.info("Recipe loaded from get by id: " + (recipe != null ? recipe.toString() : "No recipe found"));
        return recipe;

    }

    public Set<String> findIngredients(Long recipeId) {
        //read one
        RestTemplate restTemplate = new RestTemplate();
        Set<String> ingredients = restTemplate.getForObject(SERVICE_URL + "ingredientbyrecipe/" + recipeId, Set.class);
        log.info("Number of total ingredients: " + ingredients.size());
        return ingredients;

    }

    public void deleteById(Long id) {
        //read all
        log.info("Delete id: " + id);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(SERVICE_URL + "recipes/" + id);
    }

    public void updateRecipe(RecipeEntity recipeEntity) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(SERVICE_URL + "recipesupdate", recipeEntity, RecipeEntity.class);
        log.info("Recipe updated    : " + recipeEntity.toString());
    }

    public RecipeEntity testCreateSimpleOne() {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = getSimpleRecipeEntity();
        recipe = restTemplate.postForObject(SERVICE_URL + "recipes", recipe, RecipeEntity.class);
        log.info("Simple recipe created: " + recipe.toString());
        return recipe;
    }

    public RecipeEntity saveRecipeWithOCR(RecipeEntity recipeEntity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(SERVICE_URL + "recipesocr", recipeEntity, RecipeEntity.class);
    }

    public RecipeEntity saveRecipe(RecipeEntity recipeEntity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(SERVICE_URL + "recipes", recipeEntity, RecipeEntity.class);
    }

    public String findNameInRecipe(RecipeEntity recipeEntity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(SERVICE_URL + "findrecipename", recipeEntity, String.class);
    }

    public RecipeEntity createRecipeWithBinary() {
        URL url_base = RecipeAPIClient.class.getResource(recipePathIn);
        byte[] image = null;
        try {
            image = FileUtils.readFileToByteArray(new File(url_base.getPath()));
        } catch (IOException e) {
            log.error("Error while creating a Recipe with binary image from disk", e);
        }

        log.info("Calling API");
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = getSimpleRecipeEntity();
        //recipe.setBinaryDescription(image);
        RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity(image, Hash.MD5.checksum(image));
        recipe.setRecipeBinaryEntity(recipeBinaryEntity);
        //recipeBinaryEntity.setRecipe(recipe);
        recipe = restTemplate.postForObject(SERVICE_URL + "recipes", recipe, RecipeEntity.class);
        log.info("Recipe created returned by post: " + recipe.toString());
        return recipe;
    }

    public RecipeEntity addRecipeBinaryEntity(RecipeEntity recipe, boolean withOcr) {
        RecipeBinaryEntity recipeBinaryEntity = getRecipeBinaryEntity();

        RestTemplate restTemplate = new RestTemplate();
        recipe.setRecipeBinaryEntity(recipeBinaryEntity);
        if (withOcr) {
            recipe = restTemplate.postForObject(SERVICE_URL + "recipesocr", recipe, RecipeEntity.class);
        } else {
            recipe = restTemplate.postForObject(SERVICE_URL + "recipes", recipe, RecipeEntity.class);

        }
        log.info("Recipe created returned by post: " + recipe.toString());
        return recipe;
    }

    public RecipeEntity testCreateOneWithOCRInServer(boolean withOcr) {
        RecipeBinaryEntity recipeBinaryEntity = getRecipeBinaryEntity();

        log.info("Calling API");
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = getSimpleRecipeEntity();
        recipe.setRecipeBinaryEntity(recipeBinaryEntity);
        if (withOcr) {
            recipe = restTemplate.postForObject(SERVICE_URL + "recipesocr", recipe, RecipeEntity.class);
        } else {
            recipe.setAutoDescription("recette de kefta avec de la chapelure très bonne");
            recipe = restTemplate.postForObject(SERVICE_URL + "recipes", recipe, RecipeEntity.class);

        }
        log.info("Recipe created returned by post: " + recipe.toString());
/*
        try {
            FileUtils.writeByteArrayToFile(new File(recipePathOut), recipe.getBinaryDescription());
            log.info("Recipe binary written to file: " + recipePathOut);
        } catch (IOException e) {
            log.error("Error while saving recipe binary to file", e);
        }*/

        return recipe;
    }

    private RecipeBinaryEntity getRecipeBinaryEntity() {
        log.info("Reading image from: " + recipePathIn);
        URL url_base = RecipeAPIClient.class.getResource(recipePathIn);
        byte[] image = null;
        try {
            image = FileUtils.readFileToByteArray(new File(url_base.getPath()));
        } catch (IOException e) {
            log.error("Error while creating a Recipe with binary image from disk", e);
        }

        RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
        recipeBinaryEntity.setBinaryDescription(image);
        return recipeBinaryEntity;
    }

    private RecipeEntity getSimpleRecipeEntity() {
        RecipeEntity recipe = new RecipeEntity(null, "Keftas a la coriandre", "Faire des boulettes de viandes trop bonnes");
        recipe.setComment("Un commentaire sur cette recette qui est bien");
        return recipe;
    }

}
