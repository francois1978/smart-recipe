package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import smartrecipe.service.entity.RecipeEntity;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class RecipeUpdateSteps extends AbstractSteps implements En {


    public RecipeUpdateSteps() {

        Given("client wants to update recipe created before by name {string}", (String recipeName) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeName);

        });

        When("client load and update the recipe with new name, description and comment {string}", (String recipeNewName) -> {
            String urlFind = "/sr/recipesbyautodescriptionfull/" + testContext().getPayload();
            executeGetWithListResult(urlFind, RecipeEntity[].class);

            List<RecipeEntity> recipes = (List<RecipeEntity>) testContext().getResultList();
            recipes.get(0).setName(recipeNewName);
            recipes.get(0).setComment(recipeNewName);
            recipes.get(0).setDescription(recipeNewName);
            String urlUpdate = "/sr/recipes";
            executePostForObject(urlUpdate, recipes.get(0), RecipeEntity.class);
        });

        Then("client receives updated recipe with name {string}", (String recipeNewName) -> {
            RecipeEntity recipe = (RecipeEntity) testContext().getResult();
            assertTrue(recipe != null);
            assertTrue(recipe.getName().trim().equals(recipeNewName.trim()));
            assertTrue(recipe.getDescription().trim().equals(recipeNewName.trim()));
            assertTrue(recipe.getComment().trim().equals(recipeNewName.trim()));
        });


    }

}
