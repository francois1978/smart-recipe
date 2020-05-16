package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.TagEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecipeUpdateSteps extends AbstractSteps implements En {


    public RecipeUpdateSteps() {

        Given("client wants to update recipe by name {string} and add tag {string}", (String recipeName, String tagName) -> {
            testContext().reset();
            String[] parameters = new String[]{recipeName, tagName};
            super.testContext()
                    .setPayload(parameters);

        });

        Given("client wants to remove tag {string} from recipe name {string}", (String tagName, String recipeName) -> {
            testContext().reset();
            String[] parameters = new String[]{recipeName, tagName};
            super.testContext()
                    .setPayload(parameters);

        });

        Given("client wants to update recipe by name {string}", (String recipeName) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeName);

        });


        When("client load and update the recipe with tag", () -> {
            //getpayload
            String[] payLoad = (String[]) testContext().getPayload();
            String recipeName = payLoad[0];
            String tagName = payLoad[1];

            //find tag entity
            List<TagEntity> tags = loadAllTags();
            TagEntity tagEntity = tags.stream().filter(e -> e.getName().trim().equalsIgnoreCase(tagName.trim())).findFirst().get();

            //load recipe
            loadRecipeByKeyWord(recipeName);
            loadRecipeById(((List<RecipeLight>) testContext().getResultList()).get(0).getId());

            //make update with tag
            RecipeEntity recipeEntity = (RecipeEntity) testContext().getResult();
            //recipeEntity.setTags(new HashSet(Arrays.asList(new TagEntity[]{tagEntity})));
            recipeEntity.getTags().add(tagEntity);
            updateRecipe(recipeEntity);
        });

        When("client load recipe and remove tag", () -> {
            //getpayload
            String[] payLoad = (String[]) testContext().getPayload();
            String recipeName = payLoad[0];
            String tagName = payLoad[1];

            //load recipe to update
            loadRecipeByKeyWord(recipeName);
            loadRecipeById(((List<RecipeLight>) testContext().getResultList()).get(0).getId());

            //make update with tag
            RecipeEntity recipeEntity = (RecipeEntity) testContext().getResult();
            Set<TagEntity> newTagList = recipeEntity.getTags().stream().
                    filter(e -> !e.getName().trim().equalsIgnoreCase(tagName.trim())).collect(Collectors.toSet());
            recipeEntity.setTags(newTagList);
            updateRecipe(recipeEntity);
        });


        When("client load and update the recipe with new name, description and comment {string}", (String recipeNewName) -> {
            String urlFind = "/sr/recipesbyautodescriptionfull/" + testContext().getPayload();
            executeGetWithListResult(urlFind, RecipeEntity[].class);

            List<RecipeEntity> recipes = (List<RecipeEntity>) testContext().getResultList();
            recipes.get(0).setName(recipeNewName);
            recipes.get(0).setComment(recipeNewName);
            recipes.get(0).setDescription(recipeNewName);
            updateRecipe(recipes.get(0));
        });

        Then("client receives updated recipe with name {string}", (String recipeNewName) -> {
            RecipeEntity recipe = (RecipeEntity) testContext().getResult();
            assertTrue(recipe != null);
            assertEquals("Recipe name not equals", recipe.getName().trim(), (recipeNewName.trim()));
            assertEquals("Description not equal", recipe.getDescription().trim(), (recipeNewName.trim()));
            assertEquals("Comment not equal", recipe.getComment().trim(), (recipeNewName.trim()));
        });

        Then("client receives updated recipe with tag {string}", (String tagName) -> {
            RecipeEntity recipe = (RecipeEntity) testContext().getResult();
            assertTrue(recipe != null);
            assertThat("Tags list created empty", recipe.getTags(), is(not(empty())));
            assertThat("Tag exists with name " + tagName,
                    recipe.getTags().stream().filter(e -> e.getName().trim().equalsIgnoreCase(tagName.trim())).collect(Collectors.toList()),
                    is(not(empty())));
        });

        Then("client receives updated recipe with tag count {string}", (String tagCount) -> {
            RecipeEntity recipe = (RecipeEntity) testContext().getResult();
            assertTrue(recipe != null);
            assertThat("Tags list not empty", recipe.getTags(), is(not(empty())));
            assertEquals("Tags list size not good", recipe.getTags().size(), Integer.parseInt(tagCount));
        });


    }

    private List<TagEntity> loadAllTags() {
        String urlFindAllTags = "/sr/tags";
        executeGetWithListResult(urlFindAllTags, TagEntity[].class);
        List<TagEntity> tags = (List<TagEntity>) testContext().getResultList();
        assertThat("Tags list got empty", tags, is(not(empty())));
        return tags;
    }

    private void updateRecipe(RecipeEntity recipeEntity) {
        String urlUpdate = "/sr/recipes";
        executePostForObject(urlUpdate, recipeEntity, RecipeEntity.class);
    }

}
