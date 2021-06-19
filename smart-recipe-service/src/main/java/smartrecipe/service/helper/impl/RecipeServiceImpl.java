package smartrecipe.service.helper.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import smartrecipe.service.dto.AdminEntityKeysEnum;
import smartrecipe.service.dto.RecipeBinaryLight;
import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.RecipeBinaryEntity;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.TagEntity;
import smartrecipe.service.helper.*;
import smartrecipe.service.repository.RecipeRepository;
import smartrecipe.service.utils.Hash;
import smartrecipe.service.utils.ImageUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
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

    @Resource
    private AdminService adminService;

    @Resource
    private GoogleOCRDetectionService googleOCRDetectionService;

    @Value("${recipe.ocr.testmode}")
    private boolean ocrInTestMode;


    @Override
    @Transactional
    public List<RecipeLight> searchByKeyword(String keyWord, Set<TagEntity> tagEntities) {
        return recipeRepository.searchByKeyword(keyWord, tagEntities);
    }

    @Override
    public RecipeEntity newRecipeWithOCR(byte[] recipeAsByte, String recipeNameOptional) throws Exception {

        log.info("Recipe to be created with byte array input size: " + recipeAsByte.length);

        if (recipeAsByte != null) {
            RecipeEntity recipe = new RecipeEntity();

            RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(recipeAsByte);
            recipe.setRecipeBinaryEntity(recipeBinaryEntity);
            if(!StringUtils.isEmpty(recipeNameOptional)) {
                recipe.setName(recipeNameOptional);
                recipe.setNameModifiedManual(true);
            }

            decorateRecipeWithBinaryDescription(recipe);

            RecipeEntity recipeEntity = recipeRepository.save(recipe);
            log.info("Recipe created: " + recipeEntity.toString());

            return recipe;
        }
        return null;
    }


    public void decorateRecipeWithBinaryDescription(RecipeEntity recipe) throws Exception {
        //get text from image with OCR
        recipe.getRecipeBinaryEntity().setBinaryDescriptionChecksum(Hash.MD5.checksum(recipe.getRecipeBinaryEntity().getBinaryDescription()));

        adminService.checkAndIncrementGoogleAPICall(AdminEntityKeysEnum.VISION_API_CALL_COUNTER_KEY);

        String autoDescription = googleOCRDetectionService.getTextFromImage(recipe.getRecipeBinaryEntity().getBinaryDescription(), ocrInTestMode);
        recipe.setAutoDescription(autoDescription);
        //if name has not been modified manually, find name from image
        if (recipe.getName() == null || !recipe.isNameModifiedManual()) {
            String autoName = null;
            try {
                autoName = recipeIngredientService.findNameAlgo2(recipe);
            } catch (IOException e) {
                log.error("Error find name from image", e);
            }
            recipe.setName(autoName);
        }
    }

    @Override
    public RecipeEntity newRecipeWithOCR(RecipeEntity recipe) throws Exception {

        RecipeEntity recipeEntityToUpdate = mergeWithExisting(recipe);

        if (recipeEntityToUpdate.getRecipeBinaryEntity() != null) {
            decorateRecipeWithBinaryDescription(recipe);
        }

        RecipeEntity recipeEntity = recipeRepository.save(recipeEntityToUpdate);
        log.info("Recipe created: " + recipeEntity.toString());
        return recipeEntity;
    }

    @Override
    public ResponseEntity<RecipeBinaryLight> getRecipeBinaryLightById(Long id) throws IOException {

        Optional<RecipeEntity> optionalRecipeEntity = recipeRepository.findById(id);

        byte[] compressedImage = null;
        Long recipeId = null;
        String name = null;
        String webUrl = null;
        if (optionalRecipeEntity.isPresent() &&
                optionalRecipeEntity.get().getRecipeBinaryEntity() != null &&
                optionalRecipeEntity.get().getRecipeBinaryEntity().getBinaryDescription() != null) {
            RecipeEntity recipeEntity = optionalRecipeEntity.get();
            recipeId = recipeEntity.getId();
            name = recipeEntity.getName();
            optionalRecipeEntity.get().getRecipeBinaryEntity();
            compressedImage = ImageUtils.compressByteArray(recipeEntity.getRecipeBinaryEntity().getBinaryDescription());
        }

        if (optionalRecipeEntity.isPresent()) {
            webUrl = optionalRecipeEntity.get().getWebUrl();

        }
        RecipeBinaryLight recipeBinaryLight = new RecipeBinaryLight(recipeId, compressedImage, name);
        recipeBinaryLight.setWebUrl(webUrl);
        ResponseEntity responseEntity = new ResponseEntity(recipeBinaryLight, HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<RecipeEntity> getRecipeById(Long id) {
        Optional<RecipeEntity> optionalRecipeEntity = recipeRepository.findById(id);
        if (optionalRecipeEntity.isPresent()) {
            optionalRecipeEntity.get().getRecipeBinaryEntity();
            log.info("Recipe found by id: " + id + " - " + optionalRecipeEntity.get().getName());
        }
        ResponseEntity responseEntity = new ResponseEntity(optionalRecipeEntity, HttpStatus.OK);
        return responseEntity;
    }

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
