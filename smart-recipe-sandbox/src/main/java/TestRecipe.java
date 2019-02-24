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
        Assert.assertTrue(recipeApiClient.testFindAll().size() > 0);
        RecipeEntity recipe1 = recipeApiClient.testCreateSimpleOne();
        Assert.assertTrue(recipe1 != null && recipe1.getId() != null && recipe1.getName() != null && recipe1.getDescription() != null);
        RecipeEntity recipe2 = recipeApiClient.testFindOne(recipe1.getId());
        Assert.assertTrue(recipe2 != null && recipe2.getId() != null && recipe2.getName() != null && recipe2.getDescription() != null);
        RecipeEntity recipe3 = recipeApiClient.testCreateOneWithOCRInServer();
        Assert.assertTrue(recipe3 != null && recipe3.getId() != null && recipe3.getName() != null && recipe3.getDescription() != null);
        Assert.assertTrue(recipeApiClient.testByDescription("chicken").size() > 0);


    }


}