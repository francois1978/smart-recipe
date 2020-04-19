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
import org.springframework.util.CollectionUtils;
import smartrecipe.service.dto.RecipeLight;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.RecipeEntity_;
import smartrecipe.service.entity.TagEntity;
import smartrecipe.service.entity.TagEntity_;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    private final EntityManager entityManager;
    private static final Logger log = LoggerFactory.getLogger(RecipeRepositoryCustomImpl.class);


    @Override
    public List<RecipeLight> searchByKeyword(String keyWord, Set<TagEntity> tagEntities) {

        String[] stringParts = keyWord.split(" ");
        FullTextEntityManager fullTextEm = Search.getFullTextEntityManager(entityManager);

        //build bibernate search query
        QueryBuilder tweetQb = fullTextEm.getSearchFactory().buildQueryBuilder().forEntity(RecipeEntity.class).get();

        BooleanJunction booleanJunction = tweetQb.bool();
        for (String word : stringParts) {
            word = StringUtils.remove(word, " ");
            if (word.equals("")) {
                continue;
            }
            booleanJunction.should(tweetQb.keyword().fuzzy().withEditDistanceUpTo(1).onField("autoDescription").
                    matching(word).createQuery());
            booleanJunction.should(tweetQb.keyword().fuzzy().withEditDistanceUpTo(1).onField("description").
                    matching(word).createQuery());
            booleanJunction.should(tweetQb.keyword().fuzzy().withEditDistanceUpTo(1).onField("name").
                    matching(word).createQuery());
        }

        Query query = booleanJunction.createQuery();

        FullTextQuery fullTextQuery = fullTextEm.createFullTextQuery(query, RecipeEntity.class);
        fullTextQuery.setProjection("id");
        //for DEBUG scoring purpose
        //fullTextQuery.setProjection("id", ProjectionConstants.SCORE, ProjectionConstants.EXPLANATION);

        //do lucene query to get ids
        List<Object[]> results = fullTextQuery.getResultList();

        //convert result to Long list
        List<Long> idListAsLong = results.stream().map(row -> (Long) row[0]).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(idListAsLong)){
            return Collections.emptyList();
        }

        //find recipe light for ids
        Set<RecipeLight> recipeLights = findRecipeLightById(idListAsLong, tagEntities);

        //sort recipe light with original order based on lucene score
        Map<Long, RecipeLight> recipeLighById =
                recipeLights.stream().collect(Collectors.toMap(RecipeLight::getId, Function.identity()));
        List resultSortedWithLuceneScore = new ArrayList();
        for (Long id : idListAsLong) {
            RecipeLight recipeLight = recipeLighById.get(id);
            if (recipeLight != null) {
                resultSortedWithLuceneScore.add(recipeLight);
            }
        }
        return resultSortedWithLuceneScore;
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

    public Set<RecipeLight> findRecipeLightById(List<Long> ids, Set<TagEntity> tagEntities) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = criteriaBuilder.createQuery(RecipeEntity.class);
        Root<RecipeEntity> recipeRoot = query.from(RecipeEntity.class);
        final SetJoin<RecipeEntity, TagEntity> tagJoinSet = recipeRoot.join(RecipeEntity_.tags, JoinType.LEFT);

        query.select(entityManager.getCriteriaBuilder().construct(RecipeLight.class,
                recipeRoot.get(RecipeEntity_.id),
                recipeRoot.get(RecipeEntity_.name),
                recipeRoot.get(RecipeEntity_.description),
                tagJoinSet));

        //filter by recipe ids
        Expression<String> exp = recipeRoot.get("id");
        Predicate predicateIds = exp.in(ids);

        Predicate predicateTags = null;
        if (!CollectionUtils.isEmpty(tagEntities)) {
            Set<Long> tagEntityIds = tagEntities.stream().map(tag -> tag.getId()).collect(Collectors.toSet());
            //filter by tag id
            predicateTags = criteriaBuilder.and(recipeRoot.join(RecipeEntity_.tags).get(TagEntity_.id)
                    .in(tagEntityIds));
        }

        //where clause with all predicates
        Predicate andPredicate = predicateTags == null ? predicateIds : criteriaBuilder.and(predicateIds, predicateTags);
        query.where(andPredicate);
        //execute query
        List<RecipeLight> recipeLight = entityManager.createQuery(query).getResultList();

        //merge recipe with same id, with all tags merged as one single string
        Set<RecipeLight> recipeLightAsSet = mergeRecipeLights(recipeLight);
        return recipeLightAsSet;
    }



    private Set<RecipeLight> mergeRecipeLights(List<RecipeLight> recipeLight) {
        Map<Long, List<RecipeLight>> recipesById =
                recipeLight.stream().collect(Collectors.groupingBy(RecipeLight::getId));

        Set<RecipeLight> recipeLightAsSet = new HashSet<>();

        for (Map.Entry<Long, List<RecipeLight>> entry : recipesById.entrySet()) {
            RecipeLight firstRecipeLight1 = entry.getValue().get(0);
            recipeLightAsSet.add(firstRecipeLight1);
            for (RecipeLight recipeLight2 : entry.getValue()) {
                if (recipeLight2.getTagEntity() != null) {
                    firstRecipeLight1.setTagAsString(
                            firstRecipeLight1.getTagAsString() + recipeLight2.getTagEntity().getName().trim() + " / ");
                }
            }
        }
        return recipeLightAsSet;
    }
}
