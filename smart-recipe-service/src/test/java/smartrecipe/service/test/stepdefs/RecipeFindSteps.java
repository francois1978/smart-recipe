package smartrecipe.service.test.stepdefs;

import cucumber.api.java8.En;
import org.springframework.util.CollectionUtils;
import smartrecipe.service.dto.RecipeBinaryLight;
import smartrecipe.service.dto.RecipeFindParameter;
import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.TagEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RecipeFindSteps extends AbstractSteps implements En {


    public RecipeFindSteps() {


        Given("client wants to load recipe by name {string}", (String recipeName) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeName);

        });

        Given("client wants to load recipe by keyword {string}", (String recipeName) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeName);

        });

        Given("client wants to load recipe by keyword {string} and tag name {string}", (String recipeName, String tagName) -> {
            testContext().reset();

            String[] parameters = new String[]{recipeName, tagName};
            super.testContext()
                    .setPayload(parameters);

        });


        Given("client wants to load recipe by id {string}", (String recipeId) -> {
            testContext().reset();
            super.testContext()
                    .setPayload(recipeId);

        });

        When("client load the recipe", () -> {
            loadRecipeByKeyWord((String) testContext().getPayload());
        });

        When("client load the recipe by id", () -> {
            loadRecipeById(Long.parseLong((String) testContext().getPayload()));
        });

        When("client loads the light compressed recipe by keyword", () -> {
            //load by name
            loadRecipeByKeyWord((String) testContext().getPayload());
            List<RecipeLight> recipes = (List<RecipeLight>) testContext().getResultList();
            assertTrue(!CollectionUtils.isEmpty(recipes));

            //load compressed recipe by id
            String url = "/sr/recipewithbinarycompressed/" + recipes.get(0).getId();
            executeGetWithOneResult(url, RecipeBinaryLight.class);
        });

        When("client load the recipe with keyword and tag", () -> {
            //getpayload
            String[] payLoad = (String[]) testContext().getPayload();
            String recipeName = payLoad[0];
            String tagName = payLoad[1];

            //find tag entity to get tag id
            String urlFindAllTags = "/sr/tags";
            executeGetWithListResult(urlFindAllTags, TagEntity[].class);
            List<TagEntity> tags = (List<TagEntity>) testContext().getResultList();
            assertThat("Tags list got empty", tags, is(not(empty())));
            TagEntity tagEntity = tags.stream().filter(e -> e.getName().trim().equalsIgnoreCase(tagName.trim())).findFirst().get();

            //build parameter
            RecipeFindParameter recipeFindParameter = new RecipeFindParameter();
            recipeFindParameter.setDescription(recipeName);
            recipeFindParameter.setTags(new HashSet(Arrays.asList(new TagEntity[]{tagEntity})));

            //load recipe
            loadRecipeByKeyWord(recipeName);
            loadRecipeById(((List<RecipeLight>) testContext().getResultList()).get(0).getId());

        });


        Then("client receives recipe with name {string}", (String recipeName) -> {
            List<RecipeLight> recipes = (List<RecipeLight>) testContext().getResultList();

            assertTrue("Recipe list is empty", !CollectionUtils.isEmpty(recipes));
            assertTrue("Recipe has id, name or description null", recipes.stream().filter(e ->
                    (e.getId() != null && e.getId() > 0) &&
                            e.getName() != null &&
                            e.getDescription() != null).collect(Collectors.toList()).size() == recipes.size());
            assertTrue("Recipe not found with name: " + recipeName, recipes.stream().filter(e ->
                    e.getName().trim().equalsIgnoreCase(recipeName.trim())).
                    collect(Collectors.toList()).size() >= 1);
        });

        Then("client receives recipe not null", () -> {
            RecipeEntity recipe = (RecipeEntity) testContext().getResult();
            assertNotNull("Recipe null", recipe);
        });

        Then("client receives compressed light recipe with name containing {string}", (String recipeName) -> {
            RecipeBinaryLight recipeBinaryLight = (RecipeBinaryLight) testContext().getResult();
            assertNotNull(recipeBinaryLight);

            //reload by id not compressed for size comparison
            loadRecipeById(recipeBinaryLight.getId());
            RecipeEntity notCompressedRecipe = (RecipeEntity) testContext().getResult();

            assertTrue("Recipe image not compressed", recipeBinaryLight.getBinaryDescription().length < notCompressedRecipe.getRecipeBinaryEntity().getBinaryDescription().length);
            assertTrue("Recipe has not the right name", recipeBinaryLight.getName().contains(recipeName));

        });

        Then("client receives recipe with word {string} and count of {string}", (String word, String count) -> {
            List<RecipeLight> recipes = (List<RecipeLight>) testContext().getResultList();
            assertTrue(!CollectionUtils.isEmpty(recipes));
            assertTrue("Recipe list has not the right size", recipes.size() == Integer.parseInt(count));
            assertTrue("Recipe has not the word in name or description:: " + word, recipes.stream().filter(e ->
                    ((e.getName().contains(word) || e.getDescription().contains(word)))).
                    collect(Collectors.toList()).size() == recipes.size());

        });


    }

}
