package smartrecipe.service.test.servicemock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import smartrecipe.service.entity.RecipeBinaryEntity;
import smartrecipe.service.helper.GoogleOCRDetectionService;
import smartrecipe.service.helper.GoogleSheetService;
import smartrecipe.service.test.junitbdd.RecipeCreationWithOcrTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Profile("test")
@Configuration
@Slf4j
public class BddServiceMockConfiguration {


    GoogleOCRDetectionService googleOCRDetectionService;
    GoogleSheetService googleSheetService;

    public static final String RECIPE_PATH_IN = "/recipe1.jpg";
    public static final String RECIPE_PATH_IN2 = "/frite1.jpeg";
    public static final String RECIPE_PATH_IN3= "/frite2.jpeg";

    private static RecipeBinaryEntity recipeBinaryEntity;
    private static RecipeBinaryEntity recipeBinaryEntityTruncated;

    private static List<String> ingredients = Arrays.asList(new String[]{"Porc", "poivre", "carotte", "ail", "poivre"});


    @Bean
    @Primary
    public GoogleOCRDetectionService nameService() throws Exception {

        this.googleOCRDetectionService = Mockito.mock(GoogleOCRDetectionService.class);
        String ingredientAsString = ingredients.stream().collect(Collectors.joining(","));

        Mockito.when(googleOCRDetectionService.getTextFromImage(
                getRecipeBinaryEntity().getBinaryDescription(),
                true))
                .thenReturn("MOCK RECIPE TEXT DESCRIPTION." + ingredientAsString);

        Mockito.when(googleOCRDetectionService.getTextFromImage(
                getRecipeBinaryEntityTruncated().getBinaryDescription(),
                true))
                .thenReturn("MOCK RECIPE TEXT DESCRIPTION." + ingredientAsString);
        return googleOCRDetectionService;
    }

    @Bean
    @Primary
    public GoogleSheetService googleSheetService() throws Exception {

        this.googleSheetService = Mockito.mock(GoogleSheetService.class);

        Mockito.when(googleSheetService.runUpdate(
                new HashSet(ingredients),false)).thenReturn("Update done");
        return googleSheetService;
    }


    public static List<String> getIngredients() {
        return ingredients;
    }

    //TRUNCATE IMAGE for TEST purpose with H2 by default at 250 byte in the colum
    public static RecipeBinaryEntity getRecipeBinaryEntityTruncated() throws IOException {

        if(recipeBinaryEntityTruncated ==null){
            byte[] image = Arrays.copyOfRange(getRecipeBinaryEntity().getBinaryDescription(), 0, 250);
            recipeBinaryEntityTruncated = new RecipeBinaryEntity();
            recipeBinaryEntityTruncated.setBinaryDescription(getRecipeBinaryEntity().getBinaryDescription());
        }
        return recipeBinaryEntityTruncated;
    }

    public static RecipeBinaryEntity getRecipeBinaryEntity() throws IOException {
        return getRecipeBinaryEntity(RECIPE_PATH_IN);
    }

    public static RecipeBinaryEntity getRecipeBinaryEntity(String pathImage) throws IOException {
        //if (recipeBinaryEntity == null) {
            log.info("Reading image from: " + pathImage);
            URL url_base = RecipeCreationWithOcrTest.class.getResource(pathImage);
            byte[] image = null;
            try {
                image = FileUtils.readFileToByteArray(new File(url_base.getPath()));

            } catch (IOException e) {
                log.error("Error while creating a Recipe with binary image from disk", e);
                throw e;
            }

            recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(image);

        //}
        return recipeBinaryEntity;
    }

}
