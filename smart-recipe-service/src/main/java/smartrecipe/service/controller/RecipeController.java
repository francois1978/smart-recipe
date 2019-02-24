package smartrecipe.service.controller;


import smartrecipe.service.entity.RecipeEntity;
import io.swagger.annotations.ApiOperation;
import smartrecipe.service.ocr.GoogleOCRDetection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.repository.RecipeRepository;


import java.util.List;

@RestController
public class RecipeController {

    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);


    @Autowired
    private RecipeRepository recipeRepository;

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

    // @PostMapping("/recipes")
    @RequestMapping(value = "/recipes", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe.")
    RecipeEntity newRecipe(@RequestBody RecipeEntity recipe) {

        RecipeEntity recipeEntity = recipeRepository.save(recipe);
        log.info("Recipe created: " + recipeEntity.toString());
        return recipeEntity;

    }

    @RequestMapping(value = "/recipesocr", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe with OCR detection on image.")
    RecipeEntity newRecipeWithOCR(@RequestBody RecipeEntity recipe) {

        GoogleOCRDetection ocrDetection = new GoogleOCRDetection();
        String description = ocrDetection.detect(recipe.getBinaryDescription());
        recipe.setDescription(description);
        RecipeEntity recipeEntity = recipeRepository.save(recipe);
        log.info("Recipe created: " + recipeEntity.toString());
        return recipeEntity;

    }

    @RequestMapping(value = "/recipes/{id}")
    public ResponseEntity<RecipeEntity> getRecipeById(@PathVariable("id") Long id) {
        return new ResponseEntity(recipeRepository.findById(id), HttpStatus.OK);
    }

}
