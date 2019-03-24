package smartrecipe.service.repository;

import smartrecipe.service.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Long>, RecipeRepositoryCustom{

    List<RecipeEntity> findByDescriptionContainingIgnoreCase(String description);

    List<RecipeEntity> findByAutoDescriptionContainingIgnoreCase(String description);
}
