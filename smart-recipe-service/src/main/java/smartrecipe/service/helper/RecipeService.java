package smartrecipe.service.helper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import smartrecipe.service.dto.RecipeBinaryLight;
import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.TagEntity;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface RecipeService {
    RecipeEntity newRecipeWithOCR(RecipeEntity recipe) throws Exception;

    ResponseEntity<RecipeBinaryLight> getRecipeBinaryLightById(Long id) throws IOException;

    ResponseEntity<RecipeEntity> getRecipeById(Long id);

    RecipeEntity newOrUpdateRecipe(RecipeEntity recipe);

    RecipeEntity mergeWithExisting(@RequestBody RecipeEntity recipe);

    RecipeEntity checkExistingRecipe(@RequestBody RecipeEntity recipe);

    List<RecipeLight> searchByKeyword(String keyWord, Set<TagEntity> tagEntities);

    RecipeEntity newRecipeWithOCR(byte[] recipeAsByte, String recipeNameOptional) throws Exception;
}
