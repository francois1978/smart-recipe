import dataloader.clientapi.RecipeAPIClient;
import dataloader.entity.RecipeBinaryEntity;
import dataloader.entity.RecipeEntity;
import dataloader.indexing.RecipeElementIndexWrapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Hash;
import utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

        if (args == null && args.length < 1) {
            throw new RuntimeException("Missing args (1 = path to recipe image");
        }

        ImportRecipes importRecipes = new ImportRecipes(args[0]);
        //importRecipes.mergeRecipes(importRecipes.loadAllRecipes());
        //merge recipes
        // importRecipes.mergeExistingRecipes();

        //update recipe name
        //importRecipes.updateRecipeName();

        //load recipe from directory
        importRecipes.loadAllRecipesFromDirectory();

        //migrate recipe binary
        //importRecipes.migrateBinaryDescription();
    }

    public List loadAllRecipes() {
        return recipeApiClient.testFindAll();
    }

    public void mergeExistingRecipes() throws IOException {
        List<RecipeEntity> allRecipes = recipeApiClient.testFindAll();
        mergeRecipes(allRecipes);
    }

    public void updateRecipeName() throws IOException {
        long idFilter = 438;

        //load all recipes
        List<RecipeEntity> recipeEntityList = null;
        if (idFilter == 0) {
            recipeEntityList = recipeApiClient.testFindAll();
        } else {
            recipeEntityList = new ArrayList<>();
            recipeEntityList.add(recipeApiClient.testFindOne(idFilter));
        }
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

                log.info("Processing recipe id " + recipeEntity.getId());
                String name = recipeApiClient.findNameInRecipe(recipeEntity);
                log.info("Recipe id " + recipeEntity.getId() + "- Name found: " + name);
                recipeEntity.setName(name);
                recipeApiClient.saveRecipe(recipeEntity);
                return name;
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


    public void loadAllRecipesFromDirectory() throws IOException {

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

            boolean recipeExists = false;

            //load image
            byte[] image = null;
            try {
                image = FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                log.error("Error while creating a Recipe with binary image from disk", e);
            }
            //check if recipe already exists with checksum
            String MD5checkSum = Hash.MD5.checksum(image);

            List<RecipeBinaryEntity> existingRecipeBinaryList = recipeApiClient.findByChecksum(MD5checkSum);
            if (!CollectionUtils.isEmpty(existingRecipeBinaryList)) {
                log.info("Recipe already exists (skipping): " + MD5checkSum + " - list size: " + existingRecipeBinaryList.size());
                recipeExists = true;
            }

            //create recipe
            RecipeEntity recipeEntity = null;
            if (!recipeExists) {
                recipeEntity = new RecipeEntity();
                RecipeBinaryEntity recipeBinaryEntity = new RecipeBinaryEntity();
                recipeBinaryEntity.setBinaryDescription(image);
                recipeEntity.setRecipeBinaryEntity(recipeBinaryEntity);
                recipeEntity = recipeApiClient.saveRecipeWithOCR(recipeEntity);

                //add recipe to created list (for duplicate merge post process)
                recipesCreated.add(recipeEntity);
            }
            //moving file to archive
            try {
                File newFile = new File(pathtoRecipe + "/archive");
                if (newFile.exists()) {
                    //destination file already existe, delete this one
                    file.delete();
                } else {
                    FileUtils.moveFileToDirectory(file, newFile, true);
                }
            } catch (IOException e) {
                log.error("Error moving file to archive dir", e);
            }
            if (!recipeExists) {
                log.info("Recipe created with name: " + recipeEntity.getName() + ", file moved to archive directory");
                return recipeEntity.getName();
            } else {
                return "ALREADY EXISTING recipe, checksum: " + MD5checkSum;
            }
        };
    }

    public void mergeRecipes(List<RecipeEntity> recipesCreated) throws IOException {

        boolean simuMode = false;

        log.info("Running merging for recipes with 2 images and same name");
        log.info("Nummber of recipes to process: " + recipesCreated.size());
        //group recipe by name
        Map<String, List<RecipeEntity>> recipesByName =
                recipesCreated.stream().collect(Collectors.groupingBy(recipe -> {
                    String[] nameSplitted = recipe.getName().split(" ");
                    String key = Arrays.toString(Arrays.copyOfRange(nameSplitted, 0, Math.min(4, nameSplitted.length)));
                    return key;
                }));

        //iterate on list to find duplicates
        Iterator it = recipesByName.values().iterator();
        while (it.hasNext()) {
            List<RecipeEntity> recipesList = (List<RecipeEntity>) it.next();

            if (recipesList.size() == 1) continue;
            //process only case with 2 images
            if (recipesList.size() == 2) {

                //merge two images
                RecipeEntity recipe1 = recipesList.get(0);
                RecipeEntity recipe2 = recipesList.get(1);
                log.info("Merging recipes with names: " + recipe1.getName() + " / " + recipe2.getName());
                byte[] mergedImage = ImageUtils.mergeImages(recipe1.getRecipeBinaryEntity().getBinaryDescription(), recipe2.getRecipeBinaryEntity().getBinaryDescription());

                //check if merged image already exist, and delete created one in case it exists
                String MD5checkSum = Hash.MD5.checksum(mergedImage);
                List<RecipeBinaryEntity> existingRecipeBinaryList = recipeApiClient.findByChecksum(MD5checkSum);
                if (!CollectionUtils.isEmpty(existingRecipeBinaryList)) {
                    log.info("Recipe already exists (skipping): " + MD5checkSum + " - list size: " + existingRecipeBinaryList.size());
                    log.info("Deleting 2 recipes just created");
                    if (!simuMode) {
                        recipeApiClient.deleteById(recipe1.getId());
                        recipeApiClient.deleteById(recipe2.getId());
                    }
                    continue;
                }

                //modify and save merged recipe. Delete one the recipe.
                recipe1.getRecipeBinaryEntity().setBinaryDescription(mergedImage);
                recipe1.getRecipeBinaryEntity().setBinaryDescriptionChecksum(Hash.MD5.checksum(mergedImage));
                recipe1.setAutoDescription(recipe1.getAutoDescription().length() > recipe2.getAutoDescription().length() ?
                        recipe1.getAutoDescription() : recipe2.getAutoDescription());
                if (!simuMode) {
                    recipeApiClient.saveRecipe(recipe1);
                    recipeApiClient.deleteById(recipe2.getId());
                }
            }
        }
    }

}