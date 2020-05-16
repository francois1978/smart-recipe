package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import io.cucumber.datatable.DataTable;
import org.springframework.util.CollectionUtils;
import smartrecipe.service.entity.IngredientEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class IngredientSteps extends AbstractSteps implements En {


    public IngredientSteps() {


        Given("client wants to create ingredients", (DataTable recipes) -> {
            testContext().reset();
            List<IngredientEntity> ingredientList = recipes.asList(IngredientEntity.class);

            super.testContext()
                    .setPayload(ingredientList);

        });

        Given("client wants to find ingredient by name {string}", (String recipeName) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeName);

        });

        Given("client wants to find ingredient in recipe with id {string}", (String recipeId) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeId);

        });


        When("client save ingredients", () -> {
            String url = "/sr/ingredient";
            List<IngredientEntity> recipesInput = (List<IngredientEntity>) testContext().getPayload();
            executePostForObjectList(url, recipesInput, IngredientEntity.class);
            String urlRefresh = "/sr/ingredientscacherefresh";
            executeGet(urlRefresh);
        });


        When("client load ingredient for the recipe", () -> {
            String url = "/sr/ingredientbyrecipe/" + testContext().getPayload();
            executeGetWithListResult(url, String[].class);
        });


        When("client load ingredient", () -> {
            String url = "/sr/ingredient/" + testContext().getPayload();
            executeGetWithListResult(url, IngredientEntity[].class);});

        Then("client receives ingredients, list size {string}", (String count) -> {
            List<IngredientEntity> ingredients = (List<IngredientEntity>) testContext().getResultList();
            checkIngredientList(count, ingredients);
            assertTrue("Ingredient id not null", ingredients.stream().filter(e ->
                    (e.getId()!=null)).
                    collect(Collectors.toList()).size() == ingredients.size());

        });

        Then("client receives ingredients as string, list size {string}", (String count) -> {
            List<String> ingredients = (List<String>) testContext().getResultList();
            checkIngredientList(count, ingredients);

        });
    }

    private void checkIngredientList(String count, List ingredients) {
        assertTrue(!CollectionUtils.isEmpty(ingredients));
        assertTrue("Ingredient list has not the right size",
                ingredients.size() == Integer.parseInt(count));
    }

}
