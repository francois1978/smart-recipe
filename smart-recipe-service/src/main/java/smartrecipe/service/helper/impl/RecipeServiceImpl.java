package smartrecipe.service.helper.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.helper.IngredientPlateTypeCache;
import smartrecipe.service.helper.RecipeIngredientService;
import smartrecipe.service.helper.RecipeMapper;
import smartrecipe.service.helper.RecipeService;
import smartrecipe.service.repository.RecipeRepository;
import smartrecipe.service.utils.Hash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeIngredientService recipeIngredientService;

    @Autowired
    private IngredientPlateTypeCache ingredientPlateTypeCache;


    @Override
    public RecipeEntity newOrUpdateRecipe(RecipeEntity recipe) {
        if (recipe.getRecipeBinaryEntity() != null && recipe.getRecipeBinaryEntity().getBinaryDescription() != null) {
            recipe.getRecipeBinaryEntity().setBinaryDescriptionChecksum(Hash.MD5.checksum(recipe.getRecipeBinaryEntity().getBinaryDescription()));
        }

        //for new recipes with weburl, get ingredients and plate types list from autodesscription
        if (recipe.getId() == null && recipe.getWebUrl() != null && recipe.getAutoDescription() == null) {
            try {
                log.info("Generated ingredient and plate type as autodescription from web URL: " + recipe.getWebUrl());
                String httpText = loadHttpContent(recipe.getWebUrl());
                Set<String> ingredients = recipeIngredientService.findIngredientsInText(httpText, ingredientPlateTypeCache.getIngredientEntities());
                Set<String> plateTypes = recipeIngredientService.findIngredientsInText(httpText, ingredientPlateTypeCache.getPlateTypeEntities());
                Set<String> allMatchedElements = new HashSet<>();
                allMatchedElements.addAll(ingredients);
                allMatchedElements.addAll(plateTypes);

                String matchedElementsAsSting = String.join(" ", allMatchedElements);
                recipe.setAutoDescription(matchedElementsAsSting);
                log.info("Autodescription with ingredients/plate type elements count: " + allMatchedElements.size());
            } catch (IOException e) {
                String logMessage = "Unable to load text from web page, error: ";
                recipe.setAutoDescription(logMessage + e.getMessage());
                log.error(logMessage, e);
            }
        }

        RecipeEntity recipeEntityToUpdate = mergeWithExisting(recipe);

        RecipeEntity recipeEntity = recipeRepository.save(recipeEntityToUpdate);

        log.info("Recipe created or updated: " + recipeEntity.toString());
        return recipeEntity;

    }

    private String loadHttpContent(String url) throws IOException {
        HttpClient client = new org.apache.http.impl.client.DefaultHttpClient();
        String httpText = "";
        HttpGet httpGet = new HttpGet(url);

        HttpResponse execute = client.execute(httpGet);
        InputStream content = execute.getEntity().getContent();

        BufferedReader buffer = new BufferedReader(
                new InputStreamReader(content));
        String localString;
        while ((localString = buffer.readLine()) != null) {
            httpText += localString;
        }
        return httpText;
    }

    @Override
    public RecipeEntity mergeWithExisting(@RequestBody RecipeEntity recipe) {
        RecipeEntity existingRecipe = checkExistingRecipe(recipe);
        RecipeEntity recipeEntityToUpdate;

        if (existingRecipe != null) {
            recipeMapper.updateRecipe(recipe, existingRecipe);
            recipeEntityToUpdate = existingRecipe;
        } else {
            recipeEntityToUpdate = recipe;
        }
        return recipeEntityToUpdate;
    }

    @Override
    public RecipeEntity checkExistingRecipe(@RequestBody RecipeEntity recipe) {

        Optional<RecipeEntity> entityFromDB = null;

        if (recipe.getId() != null) {
            entityFromDB = recipeRepository.findById(recipe.getId());
            if (entityFromDB.isPresent()) log.info("Recipe already exist, will be updated");
        } else {
            log.info("Recipe does not exist, id: " + recipe.getId());
        }
        return (entityFromDB != null && entityFromDB.isPresent() ? entityFromDB.get() : null);
    }


}
