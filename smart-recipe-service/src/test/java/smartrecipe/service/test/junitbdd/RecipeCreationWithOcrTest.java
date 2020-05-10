package smartrecipe.service.test.junitbdd;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.helper.GoogleOCRDetectionService;
import smartrecipe.service.helper.RecipeIngredientService;
import smartrecipe.service.helper.RecipeService;
import smartrecipe.service.repository.RecipeRepository;
import smartrecipe.service.test.servicemock.GoogleOcrServiceMockConfiguration;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SrServicesApplication.class)
@Slf4j
public class RecipeCreationWithOcrTest {
/*
    @InjectMocks
    private RecipeServiceImpl recipeService;

    @Autowired
    private RecipeIngredientService ingredientService;
*/

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeIngredientService recipeIngredientService;

    @Autowired
    GoogleOCRDetectionService googleOCRDetectionService;

    @Autowired
    RecipeRepository recipeRepository;



    //@Test
    public void givenRecipeImage_whenSaveRecipeImage_thenSaveWithAutodescriptionGenerated() throws Exception {

        RecipeEntity recipeResult = createRecipeWithOcr();

        assertNotNull(recipeResult);
        assertNotNull(recipeResult.getId());
        assertNotNull(recipeResult.getAutoDescription());
        assertNotNull(recipeResult.getRecipeBinaryEntity().getBinaryDescription());
        assertTrue(recipeResult.getAutoDescription().length() > 5);
        assertTrue(recipeResult.getAutoDescription().contains("Porc soja carotte ail"));

    }

    private RecipeEntity createRecipeWithOcr() throws Exception {
        RecipeEntity recipeEntity = new RecipeEntity();
        recipeEntity.setRecipeBinaryEntity(GoogleOcrServiceMockConfiguration.getRecipeBinaryEntityTruncated());

        Mockito.when(googleOCRDetectionService.getTextFromImage(recipeEntity.getRecipeBinaryEntity().getBinaryDescription(), true))
                .thenReturn("MOCK RECIPE TEXT DESCRIPTION. Porc soja carotte ail poivre");

        RecipeEntity recipeEntityResult =  recipeService.newRecipeWithOCR(recipeEntity);
        return recipeEntityResult;
    }

    //@Test
    public void givenIngredient_whenSearchRecipeByIngredient_thenRecipeFound() throws Exception {

        createRecipeWithOcr();

        //search exact match
        List<RecipeLight> recipes = recipeService.searchByKeyword("Porc", null);
        assertNotNull(recipes);
        assertTrue(recipes.size() > 0);

        Optional<RecipeEntity> recipeEntity = recipeRepository.findById(recipes.get(0).getId());
        assertNotNull(recipeEntity.get());
        assertTrue(recipeEntity.get().getAutoDescription().contains("Porc"));
    }

    //@Test
    public void givenIngredientApproximate_whenSearchRecipeByIngredient_thenRecipeFound() throws Exception {

        createRecipeWithOcr();

        //search approximate
        List<RecipeLight> recipes = recipeService.searchByKeyword("pouvre", null);
        assertNotNull(recipes);
        assertTrue(recipes.size() > 0);

        Optional<RecipeEntity> recipeEntity = recipeRepository.findById(recipes.get(0).getId());
        assertNotNull(recipeEntity.get());
        assertTrue(recipeEntity.get().getAutoDescription().contains("poivre"));
    }




}