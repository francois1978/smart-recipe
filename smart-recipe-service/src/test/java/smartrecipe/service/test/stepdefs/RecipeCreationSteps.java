package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import io.cucumber.datatable.DataTable;
import org.springframework.util.CollectionUtils;
import smartrecipe.service.entity.RecipeEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class RecipeCreationSteps extends AbstractSteps implements En {


    public RecipeCreationSteps() {

        Given("client want to create a recipe", (DataTable recipes) -> {
            testContext().reset();
            List<RecipeEntity> recipeList = recipes.asList(RecipeEntity.class);

            // First row of DataTable has the employee attributes hence calling get(0) method.
            super.testContext()
                    .setPayload(recipeList);

        });

        When("client save recipe", () -> {
            String url = "/sr/recipes";
            List<RecipeEntity> recipesInput = (List<RecipeEntity>) testContext().getPayload();
            executePostForObjectList(url, recipesInput, RecipeEntity.class);
        });

        Then("the client receives recipes created", () -> {
            List<RecipeEntity> recipes = (List<RecipeEntity>) testContext().getResultList();

            assertTrue("Recipe list empty", !CollectionUtils.isEmpty(recipes));
            assertTrue("Recipe name, comment, description or id is null", recipes.stream().filter(e ->
                    (e.getId() != null && e.getId() > 0) &&
                            e.getName() != null &&
                            e.getDescription() != null &&
                            e.getComment() != null).collect(Collectors.toList()).size() == recipes.size());
        });


    }

}
