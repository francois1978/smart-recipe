package smartrecipe.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.entity.RecipeEntity;

import java.util.List;

public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {

    List<IngredientEntity> findByNameContainingIgnoreCase(String name);

}
