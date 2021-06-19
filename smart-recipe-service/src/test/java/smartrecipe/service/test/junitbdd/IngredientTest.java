package smartrecipe.service.test.junitbdd;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import smartrecipe.service.SrServicesApplication;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.helper.IngredientPlateTypeCache;
import smartrecipe.service.helper.IngredientsPlateTypeIndexWrapper;
import smartrecipe.service.helper.RecipeIngredientService;
import smartrecipe.service.repository.IngredientRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SrServicesApplication.class)
@Slf4j
@ActiveProfiles("test")
public class IngredientTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeIngredientService ingredientService;

    @Autowired
    private IngredientPlateTypeCache ingredientPlateTypeCache;

    @Autowired
    private IngredientsPlateTypeIndexWrapper ingredientsPlateTypeIndexWrapper;

    @Test
    public void givenText_whenSearchIngredients_thenIngredientsFound() throws IOException {
        //create ingredients
        List<String> ingredientList = Arrays.asList(new String[]{"boeuf", "carotte", "sel", "poivre", "porc"});
        ingredientList.stream().forEach(e -> createIngredient(e));
        refreshIngredientCacheAndLuceneIndex();

        Set<String> result = ingredientService.findIngredientsInText("Un texte de recette avec du porc du sel et des carottes ",
                ingredientPlateTypeCache.getIngredientEntities());

        assertThat("Recipe contains ingredient", result.toArray(), arrayContainingInAnyOrder("porc", "carotte", "sel"));
    }

    private void refreshIngredientCacheAndLuceneIndex() throws IOException {
        //refresh ingredient cache to search ingredient with lucene after
        ingredientPlateTypeCache.refreshCache();
        ingredientsPlateTypeIndexWrapper.initLuceneIndexes();
    }


    @Test
    public void givenIngredient_whenSave_thenSaveOK() {
        String ingredientName = "TEST INGREDIENT";
        IngredientEntity ingredientResult = createIngredient(ingredientName);

        assertNotNull(ingredientResult);
        assertNotNull(ingredientResult.getId());
    }

    @Test
    public void givenText_whenFindNameInText_thenNameIsOk() throws IOException, ParseException {

        //create ingredients
        List<String> ingredientList = Arrays.asList(new String[]{"boeuf", "carotte", "sel", "poivre"});
        ingredientList.stream().forEach(e -> createIngredient(e));

        //refresh ingredient cache to search ingredient with lucene after
        refreshIngredientCacheAndLuceneIndex();

        //test algo

        //build lines
        String line1 = "Ceci est un test pour detecter le nom de la recette\n ";
        line1 += "Sur la 2eme ligne pas de mot que l'algo peut detecter\n";
        String line3 = "Boeuf";
        String line3bis = "Boeuf carotte sur une seule ligne";
        String line4 = " carotte";
        String line5 = " sel";
        String line6 = " vraiment tres bon";
        String line6bis = " bon";
        String line7 = " puis la suite de la recette avec du sel du poivre du soja des\n" + "pommes de terre et du thym";
        String line7bis = " puis la suite de la recette avec de passions\n" + "et d huile de coude";

        //test 1 : enough words on first line
        String textInput1 =
                line1 +
                        line3bis + "\n" +
                        line4 + "\n" +
                        line5 + "\n" +
                        line6 + "\n" +
                        line7;

        //test 2 : enough words on n lines
        String textInput2 =
                line1 +
                        line3 +
                        line4 + "\n" +
                        line5 + "\n" +
                        line6 + "\n" +
                        line7;

        //test 3 : not more than x lines event with less than n words
        String textInput3 =
                line1 +
                        line3 +
                        line4 + "\n" +
                        line5 + "\n" +
                        line6bis + "\n" +
                        line7;

        //test 4 : no ingredient found = recipe name empty
        String textInput4 =
                line1 + "\n" +
                        line6 + "\n" +
                        line7bis;


        String recipeName1 = ingredientService.findNameAlgo2(textInput1).trim();
        String recipeName2 = ingredientService.findNameAlgo2(textInput2).trim();
        String recipeName3 = ingredientService.findNameAlgo2(textInput3).trim();
        String recipeName4 = ingredientService.findNameAlgo2(textInput4).trim();

        log.info("recipe name: " + recipeName1);
        log.info("recipe name: " + recipeName2);
        log.info("recipe name: " + recipeName3);
        log.info("recipe name: " + recipeName4);

        assertEquals(line3bis, recipeName1);
        assertEquals("Boeuf carotte  sel  vraiment tres bon", recipeName2);
        assertEquals("Boeuf carotte  sel  bon", recipeName3);
        assertEquals("", recipeName4);

    }


    private IngredientEntity createIngredient(String ingredientName) {
        IngredientEntity ingredientEntity = new IngredientEntity();
        ingredientEntity.setName(ingredientName);
        return ingredientRepository.save(ingredientEntity);
    }
}