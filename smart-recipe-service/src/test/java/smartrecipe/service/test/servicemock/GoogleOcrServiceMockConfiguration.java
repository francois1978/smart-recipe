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
import smartrecipe.service.test.junitbdd.RecipeCreationWithOcrTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Profile("test")
@Configuration
@Slf4j
public class GoogleOcrServiceMockConfiguration {


    GoogleOCRDetectionService googleOCRDetectionService;

    private static final String RECIPE_PATH_IN = "/recipe1.jpg";

    private static RecipeBinaryEntity recipeBinaryEntity;
    private static RecipeBinaryEntity recipeBinaryEntityTruncated;

    private static List<String> ingredients = Arrays.asList(new String[]{"Porc", "soja", "carotte", "ail", "poivre"});


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

    public static List<String> getIngredients() {
        return ingredients;
    }

    //TRUNCATE IMAGE for TEST purpose with H2 by default at 250 byte in the colum
    public static RecipeBinaryEntity getRecipeBinaryEntityTruncated() throws IOException {

        if(recipeBinaryEntityTruncated ==null){
            byte[] image = Arrays.copyOfRange(getRecipeBinaryEntity().getBinaryDescription(), 0, 250);
            recipeBinaryEntityTruncated = new RecipeBinaryEntity();
            recipeBinaryEntityTruncated.setBinaryDescription(image);
        }
        return recipeBinaryEntityTruncated;
    }

    public static RecipeBinaryEntity getRecipeBinaryEntity() throws IOException {
        if (recipeBinaryEntity == null) {
            log.info("Reading image from: " + RECIPE_PATH_IN);
            URL url_base = RecipeCreationWithOcrTest.class.getResource(RECIPE_PATH_IN);
            byte[] image = null;
            try {
                image = FileUtils.readFileToByteArray(new File(url_base.getPath()));

            } catch (IOException e) {
                log.error("Error while creating a Recipe with binary image from disk", e);
                throw e;
            }

            recipeBinaryEntity = new RecipeBinaryEntity();
            recipeBinaryEntity.setBinaryDescription(image);

        }
        return recipeBinaryEntity;
    }

}
