import dataloader.clientapi.RecipeAPIClient;
import dataloader.entity.RecipeEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Assert;

import java.io.IOException;
import java.util.List;

@Slf4j
public class TestRecipeWorkflow {

    private RecipeAPIClient recipeApiClient = new RecipeAPIClient();


    public static void main(String args[]) throws IOException, ParseException {

        //SpringApplication.run(ImportRecipes.class, args);

        TestRecipeWorkflow recipeWorkflow = new TestRecipeWorkflow();
        //recipeWorkflow.runFullTests();
        recipeWorkflow.runSimpleTest();
    }
    public void runSimpleTest() {
        RecipeEntity recipeEntity = recipeApiClient.createRecipeWithBinary();
        log.info(recipeEntity.toString());
        RecipeEntity recipeEntity1 = recipeApiClient.testFindOne(recipeEntity.getId());
        log.info(recipeEntity1.toString());
        List recipesList = recipeApiClient.testFindAll();
        log.info("Recipe list size:     " + recipesList.size());
    }


    public void runFullTests() {
       //create one simple recipe
        RecipeEntity recipe1 = recipeApiClient.testCreateSimpleOne();
        Assert.assertTrue(recipe1 != null &&
                recipe1.getId() != null &&
                recipe1.getName() != null &&
                recipe1.getDescription() != null &&
                recipe1.getComment() != null);

        //read recipe just created
        RecipeEntity recipe2 = recipeApiClient.testFindOne(recipe1.getId());
        Assert.assertTrue(recipe2 != null && recipe2.getId() != null && recipe2.getName() != null && recipe2.getDescription() != null);

        //find all recipe
        Assert.assertTrue(recipeApiClient.testFindAll().size() > 0);

        //create recipe with OCR
        RecipeEntity recipe3 = recipeApiClient.testCreateOneWithOCRInServer();
        Assert.assertTrue(
                recipe3 != null &&
                        recipe3.getId() != null &&
                        recipe3.getName() != null &&
                        recipe3.getDescription() != null &&
                        recipe3.getAutoDescription() != null &&
                        recipe3.getComment() != null);
        //find recipe by description
        Assert.assertTrue(recipeApiClient.testByDescription("potiron").size() > 0);

        //find recipe by auto description generated with OCR
        Assert.assertTrue(recipeApiClient.testByAutoDescription("chicken").size() > 0);

        //create and delete recipe
        RecipeEntity recipe4 = recipeApiClient.testCreateSimpleOne();
        recipeApiClient.deleteById(recipe4.getId());
        recipe4 = recipeApiClient.testFindOne(recipe4.getId());
        Assert.assertNull(recipe4);
    }


}
