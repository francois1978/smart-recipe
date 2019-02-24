package dataloader;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class RecipeAPIClient {
    private String recipePathIn = "/recipe1.jpg";
    private String recipePathOut = "recipe_" + System.currentTimeMillis() + ".jpg";
    private static final Logger log = LoggerFactory.getLogger(RecipeAPIClient.class);

    public List testFindAll() {
        //read all
        RestTemplate restTemplate = new RestTemplate();
        //List recipes = restTemplate.getForObject("http://localhost:8080/sr/recipes", List.class);
        //log.info("Number of total recipes: " + recipes.size());

        ResponseEntity<RecipeEntity[]> response = restTemplate.getForEntity(
                "http://localhost:8080/sr/recipes", RecipeEntity[].class);

        List result = Arrays.asList(response.getBody());
        log.info("Number of total recipes: " + result.size());
        return result;


    }

    public List testByDescription(String description) {
        //read all
        log.info("Get recipes by description: " + description);
        RestTemplate restTemplate = new RestTemplate();
        List recipes = restTemplate.getForObject("http://localhost:8080/sr/recipesbydescription/" + description, List.class);
        log.info("Number of total recipes: " + recipes.size());
        return recipes;

    }

    public RecipeEntity testFindOne(Long recipeId) {
        //read one
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = restTemplate.getForObject("http://localhost:8080/sr/recipes/" + recipeId, RecipeEntity.class);
        log.info("Recipe loaded from get by id: " + recipe.toString());
        return recipe;

    }

    public RecipeEntity testCreateSimpleOne() {
        //create simple recipe
        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = getSimpleRecipeEntity();
        recipe = restTemplate.postForObject("http://localhost:8080/sr/recipes", recipe, RecipeEntity.class);
        log.info("Simple recipe created: " + recipe.toString());
        return recipe;
    }


    public RecipeEntity testCreateOneWithOCRInServer() {
        log.info("Reading image from: " + recipePathIn);
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
        recipe.setBinaryDescription(image);
        recipe = restTemplate.postForObject("http://localhost:8080/sr/recipesocr", recipe, RecipeEntity.class);
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

    public Long testCreateOneWithOCRInClient() {

        byte[] image = null;
        try {
            image = FileUtils.readFileToByteArray(new File(recipePathIn));
        } catch (IOException e) {
            log.error("Error while creating a Recipe with binary image from disk", e);
        }

        log.info("Reading text from image with OCR..");
        GoogleDetection ocrUtil = new GoogleDetection();
        String description = ocrUtil.detect(recipePathIn);
        log.info("Text read result first characters: " + description.substring(0, 50));

        RestTemplate restTemplate = new RestTemplate();
        RecipeEntity recipe = getSimpleRecipeEntity();
        recipe.setDescription(description);
        recipe.setBinaryDescription(image);
        recipe = restTemplate.postForObject("http://localhost:8080/sr/recipes", recipe, RecipeEntity.class);
        log.info("Recipe created returned by post: " + recipe.toString());

        try {
            FileUtils.writeByteArrayToFile(new File(recipePathOut), recipe.getBinaryDescription());
            log.info("Recipe binary written to file: " + recipePathOut);
        } catch (IOException e) {
            log.error("Error while saving recipe binary to file", e);
        }

        return recipe.getId();
    }

    private RecipeEntity getSimpleRecipeEntity() {
        RecipeEntity recipe = new RecipeEntity(null, "Soupe au potiron", "La soupe au potiron c'est bon");
        return recipe;
    }

}
