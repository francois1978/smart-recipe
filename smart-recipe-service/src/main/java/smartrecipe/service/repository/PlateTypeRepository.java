package smartrecipe.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.entity.PlateTypeEntity;

import java.util.List;

public interface PlateTypeRepository extends JpaRepository<PlateTypeEntity, Long> {

    List<PlateTypeEntity> findByNameContainingIgnoreCase(String name);

}
