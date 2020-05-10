package smartrecipe.service.helper;

import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.SimpleEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;

public interface RecipeIngredientService {
    void resetIngredientList() throws GeneralSecurityException, IOException;

    Set<String> addIngredientToSheet(RecipeEntity recipeEntity) throws IOException, GeneralSecurityException;

    String findNameAlgo2(RecipeEntity recipeEntity) throws IOException;

    abstract Set<String> findIngredientsInText(String sourceText, List<SimpleEntity> matchList) throws IOException;
}
