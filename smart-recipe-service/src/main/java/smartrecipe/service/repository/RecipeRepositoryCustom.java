package smartrecipe.service.repository;

import smartrecipe.service.entity.RecipeLight;

import java.util.List;

public interface RecipeRepositoryCustom {

    List<RecipeLight> searchByKeyword(String keyWord);
    List<Long> findRecipeIds();
    void buildLuceneIndexes();
}
