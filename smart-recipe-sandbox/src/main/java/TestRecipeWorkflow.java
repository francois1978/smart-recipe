import dataloader.SheetsQuickstart;
import dataloader.clientapi.IngredientAPIClient;
import dataloader.clientapi.PlateTypeAPIClient;
import dataloader.clientapi.RecipeAPIClient;
import dataloader.entity.Entity;
import dataloader.entity.RecipeBinaryEntity;
import dataloader.entity.RecipeEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Assert;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class TestRecipeWorkflow {

    private RecipeAPIClient recipeApiClient = new RecipeAPIClient();


    public static void main(String args[]) throws IOException, ParseException {

        //SpringApplication.run(ImportRecipes.class, args);

        TestRecipeWorkflow recipeWorkflow = new TestRecipeWorkflow();
        recipeWorkflow.runFullTests();
        //recipeWorkflow.runSimpleTest();
        //recipeWorkflow.runFindIngredient();
    }

    public void runFindIngredient() {
        RecipeEntity recipeEntity = recipeApiClient.testFindOne(new Long(276));

        List<List<Object>> valuesToWrite = new ArrayList();

        Set<String> ingredientList = recipeApiClient.findIngredients(recipeEntity.getId());
        for (String ingredient : ingredientList) {
            log.debug("Ingredient: " + ingredient);

            List cells = new ArrayList();
            valuesToWrite.add(cells);
            cells.add(ingredient);

        }

        SheetsQuickstart sheetsQuickstart = new SheetsQuickstart();
        try {
            sheetsQuickstart.runUpdate(valuesToWrite);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runSimpleTest() {
        RecipeEntity recipeEntity = recipeApiClient.createRecipeWithBinary();
        log.info(recipeEntity.toString());
        RecipeEntity recipeEntity1 = recipeApiClient.testFindOne(recipeEntity.getId());
        log.info(recipeEntity1.toString());
    }


    public void runFullTests() {
        RecipeEntity recipe1 = null;

        try {
            //create one simple recipe
            recipe1 = recipeApiClient.testCreateSimpleOne();
            Assert.assertTrue(recipe1 != null &&
                    recipe1.getId() != null &&
                    recipe1.getName() != null &&
                    recipe1.getDescription() != null &&
                    recipe1.getComment() != null);

            //read recipe just created
            recipe1 = recipeApiClient.testFindOne(recipe1.getId());
            Assert.assertTrue(recipe1 != null && recipe1.getId() != null && recipe1.getName() != null && recipe1.getDescription() != null);

            //find all recipe
            Assert.assertTrue(recipeApiClient.testFindAll().size() > 0);

            //create recipe with OCR
            recipe1 = recipeApiClient.testCreateOneWithOCRInServer();
            Assert.assertTrue(
                    recipe1 != null &&
                            recipe1.getId() != null &&
                            recipe1.getName() != null &&
                            recipe1.getDescription() != null &&
                            recipe1.getAutoDescription() != null &&
                            recipe1.getComment() != null &&
                            recipe1.getRecipeBinaryEntity() != null &&
                            recipe1.getRecipeBinaryEntity().getBinaryDescription() != null &&
                            recipe1.getRecipeBinaryEntity().getBinaryDescriptionChecksum() != null
            );

            Assert.assertTrue(recipe1.getName().contains("Keftas"));

            //find recipe by description
            Assert.assertTrue(recipeApiClient.testByDescription("boulettes").size() > 0);

            //find recipe by auto description generated with OCR
            Assert.assertTrue(recipeApiClient.testByAutoDescription("chapelure").size() > 0);

            //find recipe by auto description generated with OCR
            Assert.assertTrue(recipeApiClient.testByAutoDescriptionFull("chapelure").size() > 0);

            //find by checksum
            List<RecipeBinaryEntity> recipeBinaryEntityList = recipeApiClient.findByChecksum(recipe1.getRecipeBinaryEntity().getBinaryDescriptionChecksum());
            Assert.assertTrue(!CollectionUtils.isEmpty(recipeBinaryEntityList));

            //find ingredients and plate type
            List<Entity> ingredientEntities = new IngredientAPIClient().findAll();
            Assert.assertTrue(ingredientEntities != null && ingredientEntities.size() > 0);
            List<Entity> plateTypeEntities = new PlateTypeAPIClient().findAll();
            Assert.assertTrue(plateTypeEntities != null && plateTypeEntities.size() > 0);

            //find and list ingredient
            Set<String> ingredients = recipeApiClient.findIngredients(recipe1.getId());
            Assert.assertTrue(ingredients != null && ingredients.size() > 5);
        } finally {
            if (recipe1 != null) {
                //delete recipe
                recipeApiClient.deleteById(recipe1.getId());
                recipe1 = recipeApiClient.testFindOne(recipe1.getId());
                Assert.assertNull(recipe1);
            }
        }

    }


}
