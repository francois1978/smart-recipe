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
        //recipeWorkflow.runFullTests(false);
        recipeWorkflow.runSimpleTest();
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
        List results = recipeApiClient.testByAutoDescriptionFull("champignons");
        log.info("Recipe list:" + results.size());
    }

    public void runFullTests(boolean withOcr) {
        RecipeEntity recipe1 = null;

        try {
            //create one simple recipe
            recipe1 = recipeApiClient.testCreateSimpleOne();
            Assert.assertTrue("Test create simple recipe", recipe1 != null &&
                    recipe1.getId() != null &&
                    recipe1.getName() != null &&
                    recipe1.getDescription() != null &&
                    recipe1.getComment() != null);

            //read recipe just created
            recipe1 = recipeApiClient.testFindOne(recipe1.getId());
            Assert.assertTrue("Test find simple recipe created", recipe1 != null && recipe1.getId() != null && recipe1.getName() != null && recipe1.getDescription() != null);

            //update recipe
            Long previousId = recipe1.getId();
            recipe1.setComment("new comment");
            recipe1.setName("new name");
            recipe1 = recipeApiClient.saveRecipe(recipe1);
            Assert.assertTrue("Test update simple recipe", previousId.equals(recipe1.getId()) &&
                    recipe1.getComment().equals("new comment") &&
                    recipe1.getName().equals("new name"));

            //find all recipe
            List<RecipeEntity> allRecipes = recipeApiClient.testFindAll();
            Assert.assertTrue("Test find all recipes", allRecipes.size() > 0);

            //check lazy loading on one recipe
            //Assert.assertTrue("Check binary not loaded on find all (lazy loading check)", allRecipes.get(0).getRecipeBinaryEntity() == null);

            //add recipe binary
            recipe1 = recipeApiClient.addRecipeBinaryEntity(recipe1, withOcr);
            Assert.assertTrue("Test that id of recipe updated has same id that previous one while created recipe binary", recipe1.getId().equals(previousId));
            Assert.assertTrue("Test recipe binary created", recipe1.getRecipeBinaryEntity() != null && recipe1.getRecipeBinaryEntity().getBinaryDescriptionChecksum() != null);

            //create recipe with OCR
            recipe1 = recipeApiClient.testCreateOneWithOCRInServer(withOcr);
            Assert.assertTrue("Test full recipe creation " + (withOcr ? "with OCR" : "without OCR"),
                    recipe1 != null &&
                            recipe1.getId() != null &&
                            recipe1.getName() != null &&
                            recipe1.getDescription() != null &&
                            (withOcr ? recipe1.getAutoDescription() != null : true) &&
                            recipe1.getComment() != null &&
                            recipe1.getRecipeBinaryEntity() != null &&
                            recipe1.getRecipeBinaryEntity().getBinaryDescription() != null &&
                            recipe1.getRecipeBinaryEntity().getBinaryDescriptionChecksum() != null
            );

            Assert.assertTrue("Test check name on full recipe creation", recipe1.getName().contains("Keftas"));

            //read recipe just created
            recipe1 = recipeApiClient.testFindOne(recipe1.getId());
            Assert.assertTrue("Test find recipe created by id", recipe1 != null && recipe1.getId() != null && recipe1.getName() != null && recipe1.getDescription() != null);


            //find recipe by description
            Assert.assertTrue("Test find by description", recipeApiClient.testByDescription("boulettes").size() > 0);
            if (withOcr) {
                //find recipe by auto description generated with OCR
                Assert.assertTrue("Test find by auto description (OCR mode)", recipeApiClient.testByAutoDescriptionFull("chapelure").size() > 0);
            }

            //find by checksum
            List<RecipeBinaryEntity> recipeBinaryEntityList = recipeApiClient.findByChecksum(recipe1.getRecipeBinaryEntity().getBinaryDescriptionChecksum());
            Assert.assertTrue("Test find by checksum", !CollectionUtils.isEmpty(recipeBinaryEntityList));

            //find ingredients and plate type
            List<Entity> ingredientEntities = new IngredientAPIClient().findAll();
            Assert.assertTrue("Test find all ingredients", ingredientEntities != null && ingredientEntities.size() > 0);
            List<Entity> plateTypeEntities = new PlateTypeAPIClient().findAll();
            Assert.assertTrue("Test find all plate type", plateTypeEntities != null && plateTypeEntities.size() > 0);

            //find and list ingredient
            Set<String> ingredients = recipeApiClient.findIngredients(recipe1.getId());
            Assert.assertTrue("Test find recipe ingredients", ingredients != null && ingredients.size() > 5);
        } finally {
            if (recipe1 != null) {
                //delete recipe
                recipeApiClient.deleteById(recipe1.getId());
                recipe1 = recipeApiClient.testFindOne(recipe1.getId());
                Assert.assertNull("Test delete recipe", recipe1);
            }
        }

    }


}
