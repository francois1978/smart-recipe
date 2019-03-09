import dataloader.RecipeAPIClient;
import dataloader.RecipeEntity;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestRecipe {

    private static final Logger log = LoggerFactory.getLogger(TestRecipe.class);


    public static void main(String args[]) {

        //SpringApplication.run(TestRecipe.class, args);

        RecipeAPIClient recipeApiClient = new RecipeAPIClient();
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