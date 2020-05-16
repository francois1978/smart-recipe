package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import org.springframework.util.CollectionUtils;
import smartrecipe.service.entity.RecipeEntity;

import java.util.List;

import static org.junit.Assert.assertTrue;


public class RecipeDeleteSteps extends AbstractSteps implements En {


    private String expectedMergedImageCheckSum;

    public RecipeDeleteSteps() {

        Given("client wants to delete all recipes to reset test data",
                () -> {
                    testContext().reset();
                });


        When("client run delete all recipes", () -> {

            //load all
            String urlGetAll = "/sr/recipesids/";
            executeGetWithListResult(urlGetAll, Long[].class);
            List<Long> recipes = (List<Long>) testContext().getResultList();

            //delete all
            recipes.stream().forEach(e -> {
                String urlDelete = "/sr/recipes/" + e;
                executeDelete(urlDelete);
            });

            //load all again for testing
            executeGetWithListResult(urlGetAll, Long[].class);

        });

        Then("client got no recipe with find all", () -> {
            List<RecipeEntity> recipes = (List<RecipeEntity>) testContext().getResultList();
            assertTrue("Recipes list not empty", CollectionUtils.isEmpty(recipes));
        });


    }
}
