package smartrecipe.service.helper;

import org.springframework.scheduling.annotation.Scheduled;
import smartrecipe.service.entity.SimpleEntity;

import java.util.List;

public interface IngredientPlateTypeCache {
    @Scheduled(fixedDelay=1800000)
    void refreshCache();

    List<SimpleEntity> getIngredientEntities();

    List<SimpleEntity> getPlateTypeEntities();
}
