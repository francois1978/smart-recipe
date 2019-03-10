package smartrecipe.service.repository;

import smartrecipe.service.entity.RecipeEntity;

import java.util.List;

public interface RecipeRepositoryCustom {

    List<RecipeEntity> searchByKeyword(String keyWord);
    void buildLuceneIndexes();
}
