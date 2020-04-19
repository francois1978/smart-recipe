package smartrecipe.service.helper;

import org.springframework.web.bind.annotation.RequestBody;
import smartrecipe.service.entity.RecipeEntity;

public interface RecipeService {
    RecipeEntity newOrUpdateRecipe(RecipeEntity recipe);

    RecipeEntity mergeWithExisting(@RequestBody RecipeEntity recipe);

    RecipeEntity checkExistingRecipe(@RequestBody RecipeEntity recipe);
}
