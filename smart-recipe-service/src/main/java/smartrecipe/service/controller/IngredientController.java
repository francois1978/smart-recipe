package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.repository.IngredientRepository;

import java.util.List;

@Slf4j
@RestController
public class IngredientController {

    @Autowired
    private IngredientRepository ingredientRepository;


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
