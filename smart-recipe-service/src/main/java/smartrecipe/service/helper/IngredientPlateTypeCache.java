package smartrecipe.service.helper;

import smartrecipe.service.entity.SimpleEntity;

import java.util.List;

public interface IngredientPlateTypeCache {
    List<SimpleEntity> getIngredientEntities();

    List<SimpleEntity> getPlateTypeEntities();
}
