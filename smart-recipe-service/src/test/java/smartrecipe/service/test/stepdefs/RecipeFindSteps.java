package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import org.springframework.util.CollectionUtils;
import smartrecipe.service.entity.RecipeEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RecipeFindSteps extends AbstractSteps implements En {


    public RecipeFindSteps() {


        Given("client wants to load recipe created before by name {string}", (String recipeName) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeName);

        });

        Given("client wants to load recipe created before by id {string}", (String recipeId) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeId);

        });

        When("client load the recipe", () -> {
            String url = "/sr/recipesbyautodescriptionfull/" + testContext().getPayload();
            executeGetWithListResult(url, RecipeEntity[].class);
        });

        When("client load the recipe by id", () -> {
            String url = "/sr/recipes/" + testContext().getPayload();
            executeGetWithOneResult(url, RecipeEntity.class);
        });

        When("client loads the light compressed recipe by name", () -> {
            //load by name
            String url = "/sr/recipesbyautodescriptionfull/" + testContext().getPayload();
            executeGetWithListResult(url, RecipeEntity[].class);
            List<RecipeEntity> recipes = (List<RecipeEntity>) testContext().getResultList();
            assertTrue(!CollectionUtils.isEmpty(recipes));

            //load compressed recipe by id
            url = "/sr/recipewithbinarycompressed/" + recipes.get(0).getId();
            executeGetWithOneResult(url, RecipeEntity.class);
        });


        Then("client receives recipe with name {string}", (String recipeName) -> {
            List<RecipeEntity> recipes = (List<RecipeEntity>) testContext().getResultList();

            assertTrue(!CollectionUtils.isEmpty(recipes));
            assertTrue(recipes.stream().filter(e ->
                    (e.getId() != null && e.getId() > 0) &&
                            e.getName() != null &&
                            e.getDescription() != null).collect(Collectors.toList()).size() == recipes.size());
            assertTrue(recipes.stream().filter(e ->
                    e.getName().trim().equalsIgnoreCase(recipeName.trim())).
                    collect(Collectors.toList()).size() >= 1);
        });

        Then("client receives recipe not null", () -> {
            RecipeEntity recipe = (RecipeEntity) testContext().getResult();
            assertNotNull(recipe);
        });

        Then("client receives compressed light recipe with name containing {string}", (String recipeName) -> {
            RecipeEntity recipe = (RecipeEntity) testContext().getResult();
            assertNotNull(recipe);

            //reload by id not compressed for size comparison
            String url = "/sr/recipes/" + testContext().getPayload();
            executeGetWithOneResult(url, RecipeEntity.class);
            RecipeEntity notCompressedRecipe = (RecipeEntity) testContext().getResult();

            assertTrue(recipe.getRecipeBinaryEntity().getBinaryDescription().length < notCompressedRecipe.getRecipeBinaryEntity().getBinaryDescription().length);
            assertTrue(recipe.getName().contains(recipeName));

        });
    }

}
