package smartrecipe.service.helper;

import org.springframework.scheduling.annotation.Scheduled;
import smartrecipe.service.helper.impl.LuceneIndexType;

import java.io.IOException;

public interface IngredientsPlateTypeIndexWrapper {
    @Scheduled(fixedDelay=1800000)
    void initLuceneIndexes() throws IOException;

    String queryByName(LuceneIndexType indexType, String name) throws IOException;
}
