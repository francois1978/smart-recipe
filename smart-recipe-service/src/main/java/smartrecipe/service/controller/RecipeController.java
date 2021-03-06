package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.dto.RecipeBinaryLight;
import smartrecipe.service.dto.RecipeFindParameter;
import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.RecipeBinaryEntity;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.helper.RecipeIngredientService;
import smartrecipe.service.helper.RecipeService;
import smartrecipe.service.repository.RecipeBinaryRepository;
import smartrecipe.service.repository.RecipeRepository;
import smartrecipe.service.utils.ImageUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;

@RestController
public class RecipeController {

    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeBinaryRepository recipeBinaryRepository;


    @Autowired
    private RecipeIngredientService recipeIngredientService;


    @Resource
    private RecipeService recipeService;

    @GetMapping("/healthcheck")
    @ApiOperation("Health check")
    String healthCheck() {
        return "Smart Recipe Alive";
    }


    @GetMapping("/buildluceneindex")
    @ApiOperation("Rebuild all lucene indexes")
    void buildLuceneIndexes() {
        recipeRepository.buildLuceneIndexes();
    }

    @GetMapping("/recipesids")
    @ApiOperation("Find all recipes ids.")
    List<Long> findAllRecipeIds() {
        return recipeRepository.findRecipeIds();
    }

    @GetMapping("/recipes")
    @ApiOperation("Find all recipes.")
    List<RecipeEntity> findAll() {
        List<RecipeEntity> result = recipeRepository.findAll();
        //boolean check  = result.get(0).checkBinaryEntityNull();
        //log.info("test null" + check);
        return result;
    }

    @GetMapping("/recipesbydescription/{description}")
    @ApiOperation("Find recipes by description key word.")
    List<RecipeEntity> findByDescription(@PathVariable("description") String description) {
        return recipeRepository.findByDescriptionContainingIgnoreCase(description);
    }

    //TODO find on child entity
    @GetMapping("/recipesbychecksum/{checksum}")
    @ApiOperation("Find recipes by binary description checksum.")
    List<RecipeBinaryEntity> findByChecksum(@PathVariable("checksum") String checksum) {
        return recipeBinaryRepository.findByBinaryDescriptionChecksum(checksum);
    }

    @GetMapping("/recipesbyautodescription/{description}")
    @ApiOperation("Find recipes searching by key work in description generated with OCR")
    List<RecipeEntity> findByAutoDescription(@PathVariable("description") String description) {
        return recipeRepository.findByAutoDescriptionContainingIgnoreCase(description);
    }

    @GetMapping("/recipesbyautodescriptionfull/{description}")
    @ApiOperation("Find recipes searching by key work in autodescription, description or name generated with OCR, using index lucene")
    List<RecipeLight> findByAutoDescriptionFull(@PathVariable("description") String description) {
        return recipeRepository.searchByKeyword(description, null);
    }

    @RequestMapping(value = "/recipesbyautodescriptionfull", method = RequestMethod.POST)
    @ApiOperation("Find recipes searching by tags and key work in description generated with OCR, using index lucene")
    List<RecipeLight> findByAutoDescriptionFullAndTages(@RequestBody RecipeFindParameter parameter) {
        List<RecipeLight> result = recipeRepository.searchByKeyword(parameter.getDescription(), parameter.getTags());
        return result;
    }

    @GetMapping("/addingredientbyrecipe/{id}")
    @ApiOperation("Find and add ingredient to external sheet")
    Set<String> addIngredients(@PathVariable("id") Long id) throws IOException, GeneralSecurityException {
        Set<String> result = recipeIngredientService.addIngredientToSheet(recipeRepository.findById(id).get());
        return result;
    }

    @GetMapping("/resetingredientlist")
    @ApiOperation("Find and add ingredient to external sheet")
    void resetIngredient() throws GeneralSecurityException, IOException {
        recipeIngredientService.resetIngredientList();
    }


    @RequestMapping(value = "/findrecipename", method = RequestMethod.POST)
    @ApiOperation("Find recipe name in auto description")
    String findNameInDescription(@RequestBody RecipeEntity recipe) {
        String name = null;
        try {
            name = recipeIngredientService.findNameAlgo2(recipe);
        } catch (IOException e) {
            log.error("Error while finding name in description", e);
        }
        return name;
    }

    @RequestMapping(value = "/recipes", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe or update existing one")
    RecipeEntity newOrUpdateRecipe(@RequestBody RecipeEntity recipe) {
        return recipeService.newOrUpdateRecipe(recipe);
    }


    @RequestMapping(value = "/recipesocr", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe with OCR detection on image.")
    RecipeEntity newRecipeWithOCR(@RequestBody RecipeEntity recipe) throws Exception {
        return recipeService.newRecipeWithOCR(recipe);
    }


    @RequestMapping(value = "/recipesbytetab", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe with OCR detection on image list")
    RecipeEntity newRecipeWithOCRInputTab(@RequestBody List<byte[]> recipeAsByte) throws Exception {

        byte[] imageMerged = ImageUtils.mergeImagesList(recipeAsByte, true, 90);
        return recipeService.newRecipeWithOCR(imageMerged, null);
    }

    @RequestMapping(value = "/recipesbytetabwithname/{recipename}", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe with OCR detection on image list wt name in input")
    RecipeEntity newRecipeWithOCRInputTab(@PathVariable("recipename") String recipeName,
                                          @RequestBody List<byte[]> recipeAsByte) throws Exception {

        byte[] imageMerged = ImageUtils.mergeImagesList(recipeAsByte, true, 90);
        return recipeService.newRecipeWithOCR(imageMerged, recipeName);
    }

    @RequestMapping(value = "/recipesbyte", method = RequestMethod.POST)
    @ApiOperation("Create a new recipe with OCR detection on image.")
    RecipeEntity newRecipeWithOCR(@RequestBody byte[] recipeAsByte) throws Exception {
        return recipeService.newRecipeWithOCR(recipeAsByte, null);
    }

    @RequestMapping(value = "/recipes/{id}")
    public ResponseEntity<RecipeEntity> getRecipeById(@PathVariable("id") Long id) {
        return recipeService.getRecipeById(id);
    }

    @RequestMapping(value = "/recipewithbinarycompressed/{id}")
    public ResponseEntity<RecipeBinaryLight> getRecipeBinaryLightById(@PathVariable("id") Long id) throws IOException {
        return recipeService.getRecipeBinaryLightById(id);
    }


    //@RequestMapping(value = "/recipes/{id}")
    @DeleteMapping("/recipes/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        recipeRepository.deleteById(id);
    }

}
