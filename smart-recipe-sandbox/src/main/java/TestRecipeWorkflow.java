import dataloader.SheetsQuickstart;
import dataloader.clientapi.IngredientAPIClient;
import dataloader.clientapi.PlateTypeAPIClient;
import dataloader.clientapi.RecipeAPIClient;
import dataloader.clientapi.TagAPIClient;
import dataloader.entity.Entity;
import dataloader.entity.RecipeBinaryEntity;
import dataloader.entity.RecipeEntity;
import dataloader.entity.TagEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Assert;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class TestRecipeWorkflow {

    private RecipeAPIClient recipeApiClient = new RecipeAPIClient();
    private TagAPIClient tagAPIClient = new TagAPIClient();

    public static void main(String args[]) throws IOException, ParseException {

        //SpringApplication.run(ImportRecipes.class, args);

        TestRecipeWorkflow recipeWorkflow = new TestRecipeWorkflow();
        recipeWorkflow.runFullTests(true);
        // recipeWorkflow.runTagTests();
        //recipeWorkflow.runFindIngredient();
    }

    public void runFindIngredient() {
        RecipeEntity recipeEntity = recipeApiClient.testFindOne(new Long(276));

        List<List<Object>> valuesToWrite = new ArrayList();

        Set<String> ingredientList = recipeApiClient.findIngredients(recipeEntity.getId());
        for (String ingredient : ingredientList) {
            log.debug("Ingredient: " + ingredient);

            List cells = new ArrayList();
            valuesToWrite.add(cells);
            cells.add(ingredient);

        }

        SheetsQuickstart sheetsQuickstart = new SheetsQuickstart();
        try {
            sheetsQuickstart.runUpdate(valuesToWrite);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runTagTests() {

        TagEntity tagEntity1 = null;

        TagEntity tagEntity2 = null;
        try {
            tagEntity1 = (TagEntity) tagAPIClient.create(tagAPIClient.buildNewEntity("light"));
            tagEntity2 = (TagEntity) tagAPIClient.create(tagAPIClient.buildNewEntity("samedi soir"));

            Assert.assertTrue("Test create tag",
                    tagEntity1 != null && tagEntity1.getId() != null && tagEntity1.getName() != null);
            List allTags = tagAPIClient.findAll();
            Assert.assertTrue("Test find all tags", allTags.size() > 1);
        } finally {
            tagAPIClient.delete(tagEntity1.getId());
            tagAPIClient.delete(tagEntity2.getId());

        }
    }


    public void runFullTests(boolean withOcr) {
        RecipeEntity recipe1 = null;
        RecipeEntity recipe2 = null;

        try {
            //create one simple recipe
            recipe1 = recipeApiClient.testCreateSimpleOne();
            Assert.assertTrue("Test create simple recipe", recipe1 != null &&
                    recipe1.getId() != null &&
                    recipe1.getName() != null &&
                    recipe1.getDescription() != null &&
                    recipe1.getComment() != null);

            //read recipe just created
            recipe1 = recipeApiClient.testFindOne(recipe1.getId());
            Assert.assertTrue("Test find simple recipe created", recipe1 != null && recipe1.getId() != null && recipe1.getName() != null && recipe1.getDescription() != null);

            //update recipe
            Long previousId = recipe1.getId();
            recipe1.setComment("new comment");
            recipe1.setName("new name");
            recipe1 = recipeApiClient.saveRecipe(recipe1);
            Assert.assertTrue("Test update simple recipe", previousId.equals(recipe1.getId()) &&
                    recipe1.getComment().equals("new comment") &&
                    recipe1.getName().equals("new name"));

            //find all recipe
            List<RecipeEntity> allRecipes = recipeApiClient.testFindAll();
            Assert.assertTrue("Test find all recipes", allRecipes.size() > 0);

            //check lazy loading on one recipe
            //Assert.assertTrue("Check binary not loaded on find all (lazy loading check)", allRecipes.get(0).getRecipeBinaryEntity() == null);

            //create new tag list
            List allTags = tagAPIClient.findAll();
            HashSet tagsSetWithFirstTagFound = new HashSet();
            tagsSetWithFirstTagFound.add(allTags.get(0));
            HashSet tagsSetWithSecondTagFound = new HashSet();
            tagsSetWithSecondTagFound.add(allTags.get(1));

            recipe1.setTags(tagsSetWithFirstTagFound);
            recipe1 = recipeApiClient.saveRecipe(recipe1);
            Assert.assertTrue("Test add tags", CollectionUtils.isNotEmpty(recipe1.getTags()));

            //update tag list
            recipe1.getTags().add((TagEntity) allTags.get(1));
            recipe1 = recipeApiClient.saveRecipe(recipe1);
            Assert.assertTrue("Test update tags",
                    CollectionUtils.isNotEmpty(recipe1.getTags()) &&
                            recipe1.getTags().size() == 2);


            //add recipe binary
            recipe1 = recipeApiClient.addRecipeBinaryEntity(recipe1, withOcr);
            Assert.assertTrue("Test that id of recipe updated has same id that previous one while created recipe binary", recipe1.getId().equals(previousId));
            Assert.assertTrue("Test recipe binary created", recipe1.getRecipeBinaryEntity() != null && recipe1.getRecipeBinaryEntity().getBinaryDescriptionChecksum() != null);

            //create recipe with OCR
            recipe2 = recipeApiClient.testCreateOneWithOCRInServer(withOcr);
            Assert.assertTrue("Test full recipe creation " + (withOcr ? "with OCR" : "without OCR"),
                    recipe2 != null &&
                            recipe2.getId() != null &&
                            recipe2.getName() != null &&
                            recipe2.getDescription() != null &&
                            (withOcr ? recipe2.getAutoDescription() != null : true) &&
                            recipe2.getComment() != null &&
                            recipe2.getRecipeBinaryEntity() != null &&
                            recipe2.getRecipeBinaryEntity().getBinaryDescription() != null &&
                            recipe2.getRecipeBinaryEntity().getBinaryDescriptionChecksum() != null
            );

            Assert.assertTrue("Test check name on full recipe creation", recipe2.getName().contains("Keftas"));


            //read recipe just created
            recipe2 = recipeApiClient.testFindOne(recipe2.getId());
            Assert.assertTrue("Test find recipe created by id",
                    recipe2 != null &&
                            recipe2.getId() != null &&
                            recipe2.getName() != null &&
                            recipe2.getDescription() != null);

            //test again add one tag
            recipe2.setTags(tagsSetWithFirstTagFound);
            recipe2 = recipeApiClient.saveRecipe(recipe2);
            Assert.assertTrue("Test add tags", CollectionUtils.isNotEmpty(recipe2.getTags()));

            //find recipe by description
            Assert.assertTrue("Test find by description",
                    recipeApiClient.testByDescription("boulettes").size() > 0);

            //find recipe by auto description generated with OCR or manually
            Assert.assertTrue("Test find by auto description",
                    recipeApiClient.testByAutoDescriptionFull("chapelure").size() > 0);

            //find recipe by auto description and tag filter
            Assert.assertTrue("Test find by auto description and tag filter",
                    recipeApiClient.findByAutoDescriptionFullAndTags("chapelure", tagsSetWithFirstTagFound).size() > 0);

            Assert.assertTrue("Test find by auto description and tag filter",
                    CollectionUtils.isEmpty(
                            recipeApiClient.findByAutoDescriptionFullAndTags("chapelure", tagsSetWithSecondTagFound)));

            //find by checksum
            List<RecipeBinaryEntity> recipeBinaryEntityList =
                    recipeApiClient.findByChecksum(recipe2.getRecipeBinaryEntity().getBinaryDescriptionChecksum());
            Assert.assertTrue("Test find by checksum",
                    !CollectionUtils.isEmpty(recipeBinaryEntityList));

            //find ingredients and plate type
            List<Entity> ingredientEntities = new IngredientAPIClient().findAll();
            Assert.assertTrue("Test find all ingredients",
                    ingredientEntities != null && ingredientEntities.size() > 0);
            List<Entity> plateTypeEntities = new PlateTypeAPIClient().findAll();
            Assert.assertTrue("Test find all plate type",
                    plateTypeEntities != null && plateTypeEntities.size() > 0);

            //find and list ingredient
            if (withOcr) {
                Set<String> ingredients = recipeApiClient.findIngredients(recipe2.getId());
                Assert.assertTrue("Test find recipe ingredients",
                        ingredients != null && ingredients.size() > 5);
            }
        } finally {
            if (recipe1 != null) {
                //delete recipe
                recipeApiClient.deleteById(recipe1.getId());
                recipe1 = recipeApiClient.testFindOne(recipe1.getId());
                Assert.assertNull("Test delete recipe", recipe1);
            }
            if (recipe2 != null) {
                //delete recipe
                recipeApiClient.deleteById(recipe2.getId());
                recipe2 = recipeApiClient.testFindOne(recipe2.getId());
                Assert.assertNull("Test delete recipe", recipe2);
            }

        }

    }


}
