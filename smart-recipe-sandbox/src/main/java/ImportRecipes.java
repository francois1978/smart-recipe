import dataloader.clientapi.RecipeAPIClient;
import dataloader.entity.RecipeBinaryEntity;
import dataloader.entity.RecipeEntity;
import dataloader.indexing.LuceneIndexType;
import dataloader.indexing.RecipeElementIndexWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Hash;
import utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class ImportRecipes {

    private static final Logger log = LoggerFactory.getLogger(ImportRecipes.class);
    private RecipeElementIndexWrapper recipeElementIndexWrapper;
    private RecipeAPIClient recipeApiClient = new RecipeAPIClient();
    private String pathtoRecipe;

    public ImportRecipes(String pathToRecipe) throws IOException, ParseException {
        this.recipeElementIndexWrapper = new RecipeElementIndexWrapper();
        this.pathtoRecipe = pathToRecipe;
    }

    public static void main2(String args[]) throws IOException, ParseException {

        if(args ==null && args.length < 1){
            throw new RuntimeException("Missing args (1 = path to recipe image");
        }

        ImportRecipes importRecipes = new ImportRecipes(args[0]);

        //update recipe name
        //importRecipes.updateRecipeName();

        //load recipe from directory
       importRecipes.loadAllRecipes();

        //migrate recipe binary
        //importRecipes.migrateBinaryDescription();
    }



    public void updateRecipeName() throws IOException {
        long idFilter = 0;

        //load all recipes
        List<RecipeEntity> recipeEntityList = recipeApiClient.testFindAll();

        //create task for // execution
        List<Callable<String>> callableList = new ArrayList();

        for (RecipeEntity recipeEntity : recipeEntityList) {
            Callable<String> callableTask = () -> {
                //check if recipe name has been modified manually
                if (recipeEntity.isNameModifiedManual()) {
                    return "Recipe name has been modified manually, skipping. Recipe name: " + recipeEntity.getName();
                }

                //filter by id if needed (debug purpose)
                //find name and save recipe
                if ((idFilter != 0 && idFilter == recipeEntity.getId()) || idFilter == 0) {
                    log.info("Processing recipe id " + recipeEntity.getId());
                    String name = findNameAlgo2(recipeEntity);
                    log.info("Recipe id " + recipeEntity.getId() + "- Name found: " + name);
                    recipeEntity.setName(name);
                    recipeApiClient.saveRecipe(recipeEntity);
                    return name;
                }
                return null;
            };
            callableList.add(callableTask);
        }

        //execute name update for recipes not modified manually
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            executor.invokeAll(callableList);
        } catch (InterruptedException e) {
            log.error("Error while executing parallel import", e);
        }
        executor.shutdown();

        this.recipeElementIndexWrapper.closeReader();

    }


    public void loadAllRecipes() throws IOException {

        File dir = new File(pathtoRecipe);
        File[] files = dir.listFiles();
        log.info("Starting import of recipes, number of file to process: " + (files != null ? files.length : 0));

        List<RecipeEntity> recipesCreated = new ArrayList<>();

        //create callable task
        List<Callable<String>> callableList = new ArrayList();
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            Callable<String> callableTask = createCallableTaskForRecipeUpload(pathtoRecipe, recipesCreated, file);
            callableList.add(callableTask);
        }
        //run recipes upload in // thread
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {

            executor.invokeAll(callableList);
        } catch (InterruptedException e) {
            log.error("Error while executing parallel import", e);
        }
        executor.shutdown();

        //merge recipe with same name
        mergeRecipes(recipesCreated);

    }

    private Callable<String> createCallableTaskForRecipeUpload(String pathtoRecipe, List<RecipeEntity> recipesCreated, File file) {
        return () -> {
            log.info("Processing file:" + file.getName());

            //load image
            byte[] image = null;
            try {
                image = FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                log.error("Error while creating a Recipe with binary image from disk", e);
            }
            //check if recipe already exists with checksum
            String MD5checkSum = Hash.MD5.checksum(image);

            RecipeEntity existingRecipeEntity = recipeApiClient.findByChecksum(MD5checkSum);
            if (existingRecipeEntity != null) {
                log.info("Recipe already exists (skipping): " + existingRecipeEntity.getName() + " - ID: " + existingRecipeEntity.getId());
                return "ALREADY EXISTING recipe: " + existingRecipeEntity.getName();
            }

            //create recipe
            RecipeEntity recipeEntity = new RecipeEntity();
            RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(image);
            recipeBinaryEntity.setBinaryDescriptionChecksum(Hash.MD5.checksum(image));
            recipeEntity = recipeApiClient.saveRecipeWithOCR(recipeEntity);

            //find name with algo du futur. And save new name.
            String name = findNameAlgo2(recipeEntity);
            log.info("Recipe name:" + name);
            recipeEntity.setName(name);
            recipeEntity = recipeApiClient.saveRecipe(recipeEntity);

            //add recipe to created list (for duplicate merge post process)
            recipesCreated.add(recipeEntity);

            //moving file to archive
            try {
                FileUtils.moveFileToDirectory(file, new File(pathtoRecipe + "/archive"), true);
            } catch (IOException e) {
                log.error("Error moving file to archive dir", e);
            }
            log.info("Recipe created with name: " + recipeEntity.getName() + ", file moved to archive directory");
            return name;
        };
    }

    private void mergeRecipes(List<RecipeEntity> recipesCreated) throws IOException {

        log.info("Running merging for recipes with 2 images and same name");
        //group recipe by name
        Map<String, List<RecipeEntity>> recipesByName = recipesCreated.stream().collect(Collectors.groupingBy(RecipeEntity::getName));

        //iterate on list to find duplicates
        Iterator it = recipesByName.values().iterator();
        while (it.hasNext()) {
            List<RecipeEntity> recipesList = (List<RecipeEntity>) it.next();
            if (recipesList.size() == 1) continue;
            if (recipesList.size() == 2) {

                //merge two images
                RecipeEntity recipe1 = recipesList.get(0);
                RecipeEntity recipe2 = recipesList.get(1);
                log.info("Merging recipes with name: " + recipe1.getName());
                byte[] mergedImage = ImageUtils.mergeImages(recipe1.getRecipeBinaryEntity().getBinaryDescription(), recipe2.getRecipeBinaryEntity().getBinaryDescription());

                //check if merged image already exist, and delete created one in case it exists
                String MD5checkSum = Hash.MD5.checksum(mergedImage);
                RecipeEntity existingRecipeEntity = recipeApiClient.findByChecksum(MD5checkSum);
                if (existingRecipeEntity != null) {
                    log.info("Recipe already exists: " + existingRecipeEntity.getName() + " - ID: " + existingRecipeEntity.getId());
                    log.info("Deleting 2 recipes just created");
                    recipeApiClient.deleteById(recipe1.getId());
                    recipeApiClient.deleteById(recipe2.getId());
                    continue;
                }

                //modify and save merged recipe. Delete one the recipe.
                recipe1.getRecipeBinaryEntity().setBinaryDescription(mergedImage);
                //TODO manage checksum on server side, check usage on set shceksum on sandbox client side
                recipe1.getRecipeBinaryEntity().setBinaryDescriptionChecksum(Hash.MD5.checksum(mergedImage));
                recipe1.setAutoDescription(recipe1.getAutoDescription() + recipe2.getAutoDescription());

                recipeApiClient.saveRecipe(recipe1);
                recipeApiClient.deleteById(recipe2.getId());
            }
        }
    }

    private String findNameAlgo2(RecipeEntity recipeEntity) throws IOException {
        String[] textSplitted = recipeEntity.getAutoDescription().split("\\n");
        String result = "";
        int i = 0;
        int countWord = 0;
        int lineWithIngredientFoundIndex = Integer.MIN_VALUE;
        for (String line : textSplitted) {

            String[] words = line.split(" ");

            //if first line with an ingredient or plate name not found find it
            if (lineWithIngredientFoundIndex == Integer.MIN_VALUE) {
                for (String word : words) {
                    if (recipeElementIndexWrapper.queryByName(LuceneIndexType.INGREDIENT, word) != null
                            || recipeElementIndexWrapper.queryByName(LuceneIndexType.PLATE_TYPE, word) != null) {
                        lineWithIngredientFoundIndex = i;
                        break;
                    }
                }
            }
            //add line to result of recipe element found and not too much lines
            if (lineWithIngredientFoundIndex >= 0 && (i - lineWithIngredientFoundIndex) < 4) {
                result = result + line + " ";
                countWord += words.length;
            }

            //break condition if enough word or lines parses following first line with ingredient
            if ((i == lineWithIngredientFoundIndex && countWord >= 3) || (countWord >= 5 || (i - lineWithIngredientFoundIndex) > 4)) {
                break;
            }

            i++;

            //if ((lineContainIngredient && countWord >= 3) || (countWord >= 7)) break;
        }

        return StringUtils.capitalize(result.toLowerCase());

    }




}