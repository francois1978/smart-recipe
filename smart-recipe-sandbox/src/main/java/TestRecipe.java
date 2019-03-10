import dataloader.RecipeAPIClient;
import dataloader.RecipeEntity;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class TestRecipe {

    private static final Logger log = LoggerFactory.getLogger(TestRecipe.class);

    public static void loadAllRecipes() throws IOException {

        String pathtoRecipe = "C:\\dev\\temp\\recipes_import";

        File dir = new File(pathtoRecipe);

        //Once you have the appropriate path, you can iterate through its contents:
        //List directory
        // si le repertoire courant est bien un repertoire
        File[] files = dir.listFiles();
        log.info("Starting import of recipes, number of file to process: " + (files != null ? files.length : 0));
        int count = 1;
        for (File file : files) {

            if(file.isDirectory()){
                continue;
            }
            log.info("Processing file: " + count++ + " / " + files.length + ", file name " + file.getName());
            //read image
            byte[] image = null;
            try {
                image = FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                log.error("Error while creating a Recipe with binary image from disk", e);
            }
            //create recipe

                RecipeEntity recipeEntity = new RecipeEntity();
                recipeEntity.setBinaryDescription(image);
                RecipeAPIClient recipeApiClient = new RecipeAPIClient();
                /*
                recipeEntity = recipeApiClient.saveRecipeWithOCR(recipeEntity);
                String[] textSplitted = recipeEntity.getAutoDescription().split("\\n");
                System.out.println(textSplitted[0]);
                recipeEntity.setName(textSplitted[0]);
                recipeApiClient.saveRecipe(recipeEntity);
*/

            //moving file to archive
            FileUtils.moveFileToDirectory(file,new File(pathtoRecipe + "/archive"), true);
            log.info("Recipe created with name: " + recipeEntity.getName() + ", file moved to archive directory");

        }
    }


    public static void main(String args[]) {

        //SpringApplication.run(TestRecipe.class, args);
        /*try {
            loadAllRecipes();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        RecipeAPIClient recipeApiClient = new RecipeAPIClient();
        recipeApiClient.rebuildLuceneIndexes();
        recipeApiClient.testByAutoDescriptionFull("framboises");

    }

    public static void runnFullTests() {
        RecipeAPIClient recipeApiClient = new RecipeAPIClient();
        //create one simple recipe
        RecipeEntity recipe1 = recipeApiClient.testCreateSimpleOne();
        Assert.assertTrue(recipe1 != null &&
                recipe1.getId() != null &&
                recipe1.getName() != null &&
                recipe1.getDescription() != null &&
                recipe1.getComment() != null);
        //read recipe just created
        RecipeEntity recipe2 = recipeApiClient.testFindOne(recipe1.getId());
        Assert.assertTrue(recipe2 != null && recipe2.getId() != null && recipe2.getName() != null && recipe2.getDescription() != null);
        //find all recipe
        Assert.assertTrue(recipeApiClient.testFindAll().size() > 0);

        //create recipe with OCR
        RecipeEntity recipe3 = recipeApiClient.testCreateOneWithOCRInServer();
        Assert.assertTrue(
                recipe3 != null &&
                        recipe3.getId() != null &&
                        recipe3.getName() != null &&
                        recipe3.getDescription() != null &&
                        recipe3.getAutoDescription() != null &&
                        recipe3.getComment() != null);
        //find recipe by description
        Assert.assertTrue(recipeApiClient.testByDescription("potiron").size() > 0);

        //find recipe by auto description generated with OCR
        Assert.assertTrue(recipeApiClient.testByAutoDescription("chicken").size() > 0);

        //create and delete recipe
        RecipeEntity recipe4 = recipeApiClient.testCreateSimpleOne();
        recipeApiClient.deleteById(recipe4.getId());
        recipe4 = recipeApiClient.testFindOne(recipe4.getId());
        Assert.assertNull(recipe4);
    }


}