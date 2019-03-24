package smartrecipe.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.entity.PlateTypeEntity;
import smartrecipe.service.entity.SimpleEntity;
import smartrecipe.service.repository.IngredientRepository;
import smartrecipe.service.repository.PlateTypeRepository;

import java.util.List;

//TODO refesh cache strategy to implement
@Service
@Slf4j
public class IngredientPlateTypeCache {

    private List ingredientEntities;
    private List plateTypeEntities;

    private final IngredientRepository ingredientRepository;
    private final PlateTypeRepository plateTypeRepository;

    @Autowired
    public IngredientPlateTypeCache(IngredientRepository ingredientRepository, PlateTypeRepository plateTypeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.plateTypeRepository = plateTypeRepository;
        init();
    }

    private void init() {
        List<IngredientEntity> ingredientRepositoryList = ingredientRepository.findAll();
        this.ingredientEntities = ingredientRepositoryList;
        log.info("Ingredient list loaded, size: " + ingredientRepositoryList.size());

        List<PlateTypeEntity> plateTypeEntities = plateTypeRepository.findAll();
        this.plateTypeEntities = plateTypeEntities;
        log.info("Plate type list loaded, size: " + plateTypeEntities.size());
    }

    public List<SimpleEntity> getIngredientEntities() {
        return ingredientEntities;
    }

    public List<SimpleEntity> getPlateTypeEntities() {
        return plateTypeEntities;
    }
}
