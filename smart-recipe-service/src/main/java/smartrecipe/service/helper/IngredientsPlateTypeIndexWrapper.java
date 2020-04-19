package smartrecipe.service.helper;

import smartrecipe.service.helper.impl.LuceneIndexType;

import java.io.IOException;

public interface IngredientsPlateTypeIndexWrapper {
    String queryByName(LuceneIndexType indexType, String name) throws IOException;
}
