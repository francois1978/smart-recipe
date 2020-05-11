package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import smartrecipe.service.entity.RecipeBinaryEntity;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.test.servicemock.GoogleOcrServiceMockConfiguration;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


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
            executePostForObject(url, GoogleOcrServiceMockConfiguration.getRecipeBinaryEntityTruncated().getBinaryDescription(),
                    RecipeEntity.class);
        });


        Then("the client receives recipes created with autodescription from image text", () -> {
            RecipeEntity recipeResult = (RecipeEntity) testContext().getResult();

            checkRecipeResult(recipeResult);

        });



    }

    private void buildAndPostRecipeEntityForOcr(String url) throws IOException {
        String recipeName = (String) testContext().getPayload();
        RecipeBinaryEntity recipeBinaryEntity = GoogleOcrServiceMockConfiguration.getRecipeBinaryEntityTruncated();
        RecipeEntity recipeEntity = new RecipeEntity();
        recipeEntity.setRecipeBinaryEntity(recipeBinaryEntity);
        recipeEntity.setName(recipeName);
        recipeEntity.setNameModifiedManual(true);
        executePostForObject(url, recipeEntity, RecipeEntity.class);
    }

    private void checkRecipeResult(RecipeEntity recipeResult) {
        assertNotNull(recipeResult);
        assertNotNull(recipeResult.getId());
        assertNotNull(recipeResult.getAutoDescription());
        assertNotNull(recipeResult.getRecipeBinaryEntity().getBinaryDescription());
        assertTrue(recipeResult.getAutoDescription().length() > 5);
        List<String> ingredientsToCheck = GoogleOcrServiceMockConfiguration.getIngredients();
        assertTrue(recipeResult.getAutoDescription().contains(ingredientsToCheck.get(0)));
        assertTrue(recipeResult.getAutoDescription().contains(ingredientsToCheck.get(1)));
    }

}
