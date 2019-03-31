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

//max id in prod asof 24 03 = 431
    public static void main2(String args[]) throws IOException, ParseException {

        RecipeBinaryTransfer recipeBinaryTransfer = new RecipeBinaryTransfer();

        //migrate recipe binary
        recipeBinaryTransfer.migrateBinaryDescription();
    }

    public void migrateBinaryDescription(){

        List<Integer> recipeIds = recipeApiClient.findAllRecipeIds();
        //recipeIds.clear();
        //recipeIds.add(205);
        for(Integer recipeId: recipeIds){
            log.info("Processing recipe: " + recipeId);
            RecipeEntity recipeEntity = recipeApiClient.testFindOne(new Long(recipeId));
           // log.info("Binary description size: " + reci);
            RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(recipeEntity.getBinaryDescription());
            recipeBinaryEntity.setBinaryDescriptionChecksum(recipeBinaryEntity.getBinaryDescriptionChecksum());
            recipeEntity.setRecipeBinaryEntity(recipeBinaryEntity);
            RecipeEntity recipeEntity1 = recipeApiClient.saveRecipe(recipeEntity);
            log.info("\tSaved: " + recipeEntity1);
        }

    }


}
