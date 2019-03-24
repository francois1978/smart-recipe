package smartrecipe.service.repository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrecipe.service.entity.RecipeEntity;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    private final EntityManager entityManager;
    private static final Logger log = LoggerFactory.getLogger(RecipeRepositoryCustomImpl.class);


    @Override
    public List<RecipeEntity> searchByKeyword(String keyWord) {

        String[] stringParts = keyWord.split(" ");
        FullTextEntityManager fullTextEm = Search.getFullTextEntityManager(entityManager);

        QueryBuilder tweetQb = fullTextEm.getSearchFactory().buildQueryBuilder().forEntity(RecipeEntity.class).get();

        //Query fullTextQuery = tweetQb.keyword().fuzzy().onField("autoDescription").matching(keyWord + "*").createQuery();

        BooleanJunction booleanJunction = tweetQb.bool();
        for (String word : stringParts) {
            word = StringUtils.remove(word, " ");
            if(word.equals("")){
                continue;
            }
            booleanJunction.should(tweetQb.keyword().fuzzy().onField("autoDescription").
                    matching(word).createQuery());
        }


        List results = fullTextEm.createFullTextQuery(booleanJunction.createQuery()).getResultList();
        return results;
    }

    @Override
    public void buildLuceneIndexes() {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            log.error("Error while creating lucene index", e);
        }
        log.info("Lucene indexes rebuilt");
    }
}
