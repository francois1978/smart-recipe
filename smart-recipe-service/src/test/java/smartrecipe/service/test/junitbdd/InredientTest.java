package smartrecipe.service.test.junitbdd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import smartrecipe.service.SrServicesApplication;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.helper.RecipeIngredientService;
import smartrecipe.service.repository.IngredientRepository;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SrServicesApplication.class)
public class InredientTest {
  
    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeIngredientService ingredientService;
 
    @Test
    public void givenIngredient_whenSave_thenSaveOK() {
        IngredientEntity ingredientEntity = new IngredientEntity();
        ingredientEntity.setName("TEST INGREDIENT");

        IngredientEntity ingredientResult = ingredientRepository.save(ingredientEntity);

        assertNotNull(ingredientResult);
        assertNotNull(ingredientResult.getId());
    }


}