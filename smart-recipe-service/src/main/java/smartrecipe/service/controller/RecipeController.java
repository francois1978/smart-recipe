package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.entity.RecipeBinaryEntity;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.ocr.GoogleOCRDetection;
import smartrecipe.service.repository.RecipeRepository;
import smartrecipe.service.utils.Hash;

import java.util.List;
import java.util.Optional;

@RestController
public class RecipeController {

    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    @Autowired
    private RecipeRepository recipeRepository;

    @GetMapping("/buildluceneindex")
    @ApiOperation("Rebuild all lucene indexes")
    void buildLuceneIndexes() {
        recipeRepository.buildLuceneIndexes();
    }

    @GetMapping("/recipes")
    @ApiOperation("Find all recipes.")
    List<RecipeEntity> all() {
        return recipeRepository.findAll();
    }

    @GetMapping("/recipesbydescription/{description}")
    @ApiOperation("Find recipes by description key word.")
    List<RecipeEntity> findByDescription(@PathVariable("description") String description) {
        return recipeRepository.findByDescriptionContainingIgnoreCase(description);
    }
    //TODO find on child entity
    @GetMapping("/recipesbychecksum/{checksum}")
    @ApiOperation("Find recipes by binary description checksum.")
    RecipeEntity findByChecksum(@PathVariable("checksum") String checksum) {
        return recipeRepository.findByBinaryDescriptionChecksum(checksum);
    }

    @GetMapping("/recipesbyautodescription/{description}")
    @ApiOperation("Find recipes searching by key work in description generated with OCR")
    List<RecipeEntity> findByAutoDescription(@PathVariable("description") String description) {
        return recipeRepository.findByAutoDescriptionContainingIgnoreCase(description);
    }

    @GetMapping("/recipesbyautodescriptionfull/{description}")
    @ApiOperation("Find recipes searching by key work in description generated with OCR")
    List<RecipeEntity> findByAutoDescriptionFull(@PathVariable("description") String description) {
        return recipeRepository.searchByKeyword(description);
    }

    // @PostMapping("/recipes")
    @RequestMapping(value = "/recipes", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe.")
    RecipeEntity newRecipe(@RequestBody RecipeEntity recipe) {

        //recipe.setBinaryDescriptionChecksum(Hash.MD5.checksum(recipe.getBinaryDescription()));
        RecipeEntity recipeEntity = recipeRepository.save(recipe);
        if (recipe.getRecipeBinaryEntity() != null) {
            recipe.getRecipeBinaryEntity().setBinaryDescriptionChecksum(Hash.MD5.checksum(recipe.getRecipeBinaryEntity().getBinaryDescription()));
        }
        log.info("Recipe created: " + recipeEntity.toString());
        return recipeEntity;

    }

    @RequestMapping(value = "/recipesocr", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe with OCR detection on image.")
    RecipeEntity newRecipeWithOCR(@RequestBody RecipeEntity recipe) {

        if (recipe.getRecipeBinaryEntity() != null) {
            recipe.getRecipeBinaryEntity().setBinaryDescriptionChecksum(Hash.MD5.checksum(recipe.getRecipeBinaryEntity().getBinaryDescription()));
            GoogleOCRDetection ocrDetection = new GoogleOCRDetection();
            String autoDescription = ocrDetection.detect(recipe.getRecipeBinaryEntity().getBinaryDescription());
            recipe.setAutoDescription(autoDescription);
        }

        RecipeEntity recipeEntity = recipeRepository.save(recipe);
        log.info("Recipe created: " + recipeEntity.toString());
        return recipeEntity;

    }

    @RequestMapping(value = "/recipesbyte", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe with OCR detection on image.")
    String newRecipeWithOCR(@RequestBody byte[] recipeAsByte) {

        if (recipeAsByte != null) {
            RecipeEntity recipe = new RecipeEntity();

            RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(recipeAsByte);
            recipeBinaryEntity.setBinaryDescriptionChecksum(Hash.MD5.checksum(recipeAsByte));
            recipe.setRecipeBinaryEntity(recipeBinaryEntity);

            GoogleOCRDetection ocrDetection = new GoogleOCRDetection();
            String autoDescription = ocrDetection.detect(recipeAsByte);
            recipe.setAutoDescription(autoDescription);
            RecipeEntity recipeEntity = recipeRepository.save(recipe);
            log.info("Recipe created: " + recipeEntity.toString());
            return recipeEntity.getAutoDescription();

        }
        return "No recipe created, input byte array null";
    }

    @RequestMapping(value = "/recipes/{id}")
    public ResponseEntity<RecipeEntity> getRecipeById(@PathVariable("id") Long id) {
        Optional<RecipeEntity> optionalRecipeEntity = recipeRepository.findById(id);
        if(optionalRecipeEntity.get()!=null){
            optionalRecipeEntity.get().getRecipeBinaryEntity();
        }
        ResponseEntity responseEntity = new ResponseEntity(optionalRecipeEntity, HttpStatus.OK);

        return responseEntity;
    }


    //@RequestMapping(value = "/recipes/{id}")
    @DeleteMapping("/recipes/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        recipeRepository.deleteById(id);
    }

}
