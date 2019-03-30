package smartrecipe.service.helper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import smartrecipe.service.entity.RecipeEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
//s@Service
public interface RecipeMapper {

    void updateRecipe(RecipeEntity recipeSource, @MappingTarget RecipeEntity recipeTarget);
}