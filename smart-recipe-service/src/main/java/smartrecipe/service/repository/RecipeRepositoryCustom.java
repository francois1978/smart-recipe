package smartrecipe.service.repository;

import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.TagEntity;

import java.util.List;
import java.util.Set;

public interface RecipeRepositoryCustom {

    List<RecipeLight> searchByKeyword(String keyWord, Set<TagEntity> tagEntities);

    List<Long> findRecipeIds();

    void buildLuceneIndexes();
}
