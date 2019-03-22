import dataloader.clientapi.RecipeAPIClient;
import dataloader.entity.RecipeBinaryEntity;
import dataloader.entity.RecipeEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

@Slf4j
public class RecipeBinaryTransfer {

    private RecipeAPIClient recipeApiClient = new RecipeAPIClient();


    public static void main(String args[]) throws IOException, ParseException {

        RecipeBinaryTransfer recipeBinaryTransfer = new RecipeBinaryTransfer();

        //migrate recipe binary
        recipeBinaryTransfer.migrateBinaryDescription();
    }

    public void migrateBinaryDescription(){

        List<RecipeEntity> recipeEntities = recipeApiClient.testFindAll();
        for(RecipeEntity recipeEntity : recipeEntities){
            log.info("Processing recipe: " + recipeEntity.getName());
            RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(recipeEntity.getBinaryDescription());
            recipeBinaryEntity.setBinaryDescriptionChecksum(recipeBinaryEntity.getBinaryDescriptionChecksum());
            recipeEntity.setRecipeBinaryEntity(recipeBinaryEntity);
            RecipeEntity recipeEntity1 = recipeApiClient.saveRecipe(recipeEntity);
            log.info("\tSaved: " + recipeEntity1);
        }

    }


}
