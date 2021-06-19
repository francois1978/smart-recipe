package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import smartrecipe.service.entity.RecipeBinaryEntity;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.test.servicemock.BddServiceMockConfiguration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;


public class RecipeCreationWithOcrSteps extends AbstractSteps implements En {


    private String expectedMergedImageCheckSum;

    public RecipeCreationWithOcrSteps() {

        Given("client wants to create a recipe from image and name {string}",
                (String recipeName) -> {
                    testContext().reset();

                    super.testContext()
                            .setPayload(recipeName);

                });


        When("client save recipe with recipe binary entity", () -> {
            String url = "/sr/recipesocr";
            buildAndPostRecipeEntityForOcr(url);
        });

        When("client save recipe with image as bytes array", () -> {
            String url = "/sr/recipesbyte";
            executePostForObject(url, BddServiceMockConfiguration.getRecipeBinaryEntityTruncated().getBinaryDescription(),
                    RecipeEntity.class);
        });

        When("client save recipe with image as bytes array list with recipe name", () -> {
            String url = "/sr/recipesbytetabwithname/" + super.testContext().getPayload();
            executePostForObject(url, Collections.singleton(
                    BddServiceMockConfiguration.getRecipeBinaryEntityTruncated().getBinaryDescription()),
                    RecipeEntity.class);
        });


        Then("the client receives recipes created with autodescription from image text", () -> {
            RecipeEntity recipeResult = (RecipeEntity) testContext().getResult();
            checkRecipeResult(recipeResult);
        });

        Then("the client receives recipes created with autodescription from image text and name {string}", (String recipeName) -> {
            RecipeEntity recipeResult = (RecipeEntity) testContext().getResult();
            checkRecipeResult(recipeResult);
            assertEquals("Recipe name is the one filled by used",
                    recipeResult.getName(),
                    recipeName);
        });

    }

    private void buildAndPostRecipeEntityForOcr(String url) throws IOException {
        String recipeName = (String) testContext().getPayload();
        RecipeBinaryEntity recipeBinaryEntity = BddServiceMockConfiguration.getRecipeBinaryEntityTruncated();
        RecipeEntity recipeEntity = new RecipeEntity();
        recipeEntity.setRecipeBinaryEntity(recipeBinaryEntity);
        recipeEntity.setName(recipeName);
        recipeEntity.setNameModifiedManual(true);
        executePostForObject(url, recipeEntity, RecipeEntity.class);
    }

    private void checkRecipeResult(RecipeEntity recipeResult) {
        assertNotNull("Recipe is null", recipeResult);
        assertNotNull(recipeResult.getId());
        assertNotNull(recipeResult.getAutoDescription());
        assertNotNull(recipeResult.getRecipeBinaryEntity().getBinaryDescription());
        assertTrue("Recipe autodescription has a too small size", recipeResult.getAutoDescription().length() > 5);
        List<String> ingredientsToCheck = BddServiceMockConfiguration.getIngredients();
        assertTrue("Recipe not containing ingredient " + ingredientsToCheck.get(0), recipeResult.getAutoDescription().contains(ingredientsToCheck.get(0)));
        assertTrue("Recipe not containing ingredient " + ingredientsToCheck.get(1), recipeResult.getAutoDescription().contains(ingredientsToCheck.get(1)));
    }

}
