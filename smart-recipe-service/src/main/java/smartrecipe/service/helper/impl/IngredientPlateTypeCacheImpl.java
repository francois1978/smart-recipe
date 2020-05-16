package smartrecipe.service.helper.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.entity.PlateTypeEntity;
import smartrecipe.service.entity.SimpleEntity;
import smartrecipe.service.helper.IngredientPlateTypeCache;
import smartrecipe.service.repository.IngredientRepository;
import smartrecipe.service.repository.PlateTypeRepository;

import java.util.List;

//TODO refesh cache strategy to implement
@Service
@Slf4j
public class IngredientPlateTypeCacheImpl implements IngredientPlateTypeCache {

    private List ingredientEntities;
    private List plateTypeEntities;

    private final IngredientRepository ingredientRepository;
    private final PlateTypeRepository plateTypeRepository;

    @Autowired
    public IngredientPlateTypeCacheImpl(IngredientRepository ingredientRepository, PlateTypeRepository plateTypeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.plateTypeRepository = plateTypeRepository;
        initCache();
    }

    private synchronized void initCache() {
        List<IngredientEntity> ingredientRepositoryList = ingredientRepository.findAll();
        this.ingredientEntities = ingredientRepositoryList;
        log.debug("Ingredient list loaded, size: " + ingredientRepositoryList.size());

        List<PlateTypeEntity> plateTypeEntities = plateTypeRepository.findAll();
        this.plateTypeEntities = plateTypeEntities;
        log.debug("Plate type list loaded, size: " + plateTypeEntities.size());
    }

    @Override
    @Scheduled(fixedDelay=1800000)
    public void refreshCache(){
        initCache();
    }


    @Override
    public List<SimpleEntity> getIngredientEntities() {
        return ingredientEntities;
    }

    @Override
    public List<SimpleEntity> getPlateTypeEntities() {
        return plateTypeEntities;
    }
}
