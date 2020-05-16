package smartrecipe.service.helper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public interface GoogleSheetService {
    void resetIngredientList() throws GeneralSecurityException, IOException;

    String runUpdate(Set<String> ingredientList, boolean concatToExisting) throws GeneralSecurityException, IOException;
}
