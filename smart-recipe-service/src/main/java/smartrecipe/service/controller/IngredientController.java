package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.helper.RecipeIngredientHelper;
import smartrecipe.service.repository.IngredientRepository;
import smartrecipe.service.repository.RecipeRepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
public class IngredientController {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;


    @Autowired
    private RecipeIngredientHelper recipeIngredientHelper;

    @GetMapping("/ingredientbyrecipe/{id}")
    @ApiOperation("Find ingredient of a recipe")
    Set<String> finRecipeIngredients(@PathVariable("id") Long id) {

        Optional<RecipeEntity> recipeEntity = recipeRepository.findById(id);
        if (!recipeEntity.isPresent() || recipeEntity.get().getAutoDescription() == null){
            return new HashSet();
        }
            Set ingredients = null;
        try {
            ingredients = recipeIngredientHelper.findIngredientsByRecipe(recipeEntity.get());
        } catch (IOException e) {
            log.error("Unable to search recipe ingredients", e);
        }
        return ingredients;
    }

    @GetMapping("/ingredients")
    @ApiOperation("Find all ingredients.")
    List<IngredientEntity> all() {
        return ingredientRepository.findAll();
    }

    @GetMapping("/ingredient/{name}")
    @ApiOperation("Find ingredient by name")
    List<IngredientEntity> findByDescription(@PathVariable("name") String name) {
        List<IngredientEntity> ingredientEntityList = ingredientRepository.findByNameContainingIgnoreCase(name);
        log.info("Number of ingredients loaded: " + ingredientEntityList.size());
        return ingredientEntityList;
    }

    @RequestMapping(value = "/ingredient", method = RequestMethod.POST)
    @ApiOperation("Create a new ingredient.")
    IngredientEntity newRecipe(@RequestBody IngredientEntity ingredient) {

        IngredientEntity ingredientEntity = ingredientRepository.save(ingredient);
        log.info("Ingredient created: " + ingredient.toString());
        return ingredientEntity;

    }

    @RequestMapping(value = "/ingredient/{id}")
    @ApiOperation("Get ingredient by id")
    public ResponseEntity<IngredientEntity> getIngredientById(@PathVariable("id") Long id) {
        return new ResponseEntity(ingredientRepository.findById(id), HttpStatus.OK);
    }


}
