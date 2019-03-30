package smartrecipe.service.repository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.RecipeLight;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    private final EntityManager entityManager;
    private static final Logger log = LoggerFactory.getLogger(RecipeRepositoryCustomImpl.class);


    @Override
    public List<RecipeLight> searchByKeyword(String keyWord) {

        String[] stringParts = keyWord.split(" ");
        FullTextEntityManager fullTextEm = Search.getFullTextEntityManager(entityManager);

        QueryBuilder tweetQb = fullTextEm.getSearchFactory().buildQueryBuilder().forEntity(RecipeEntity.class).get();

        //Query fullTextQuery = tweetQb.keyword().fuzzy().onField("autoDescription").matching(keyWord + "*").createQuery();

        BooleanJunction booleanJunction = tweetQb.bool();
        for (String word : stringParts) {
            word = StringUtils.remove(word, " ");
            if (word.equals("")) {
                continue;
            }
            booleanJunction.should(tweetQb.keyword().fuzzy().withEditDistanceUpTo(1).onField("autoDescription").
                    matching(word).createQuery());
        }

        Query query = booleanJunction.createQuery();

        FullTextQuery fullTextQuery = fullTextEm.createFullTextQuery(query, RecipeEntity.class);
        fullTextQuery.setProjection("id");

        List<Object[]> results = fullTextQuery.getResultList();
        List<Long> idListAsLong = results.stream().map(row -> (Long) row[0]).collect(Collectors.toList());

        List<RecipeLight> recipeLights = findRecipeLightById(idListAsLong);

        return recipeLights;
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

    public List<Long> findRecipeIds() {

        CriteriaQuery query = entityManager.getCriteriaBuilder().createQuery(RecipeEntity.class);
        Root recipeRoot = query.from(RecipeEntity.class);
        query.select(entityManager.getCriteriaBuilder().construct(Long.class, recipeRoot.get("id")));
        List ids = entityManager.createQuery(query).getResultList();
        return ids;
    }

    public List<RecipeLight> findRecipeLightById(List<Long> ids) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = criteriaBuilder.createQuery(RecipeEntity.class);
        Root recipeRoot = query.from(RecipeEntity.class);
        query.select(entityManager.getCriteriaBuilder().construct(RecipeLight.class,
                recipeRoot.get("id"),
                recipeRoot.get("name"),
                recipeRoot.get("description")));
        Expression<String> exp = recipeRoot.get("id");
        Predicate predicate = exp.in(ids);
        query.where(predicate);
        //query.where(criteriaBuilder.in(recipeRoot.get("id"), id));
        List<RecipeLight> recipeLight = entityManager.createQuery(query).getResultList();
        return recipeLight;
    }
}
