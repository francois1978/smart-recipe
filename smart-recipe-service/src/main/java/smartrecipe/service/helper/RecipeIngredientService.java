package smartrecipe.service.helper;

import smartrecipe.service.entity.SimpleEntity;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface RecipeIngredientService {
    Set<String> findIngredientsInText(String sourceText, List<SimpleEntity> matchList) throws IOException;
}
