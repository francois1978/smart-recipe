package smartrecipe.service.helper.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smartrecipe.service.entity.RecipeEntity;
import smartrecipe.service.entity.SimpleEntity;
import smartrecipe.service.helper.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class RecipeIngredientImpl implements RecipeIngredientService {

    public static final int RECIPE_NAME_FIND_ALGO_WORD_COUNT_ON_FIRST_LINE = 3;
    public static final int RECIPE_NAME_FIND_ALGO_WORD_COUNT_FROM_FIRST_LINE = 5;
    private IngredientPlateTypeCache ingredientPlateTypeCache;
    private IngredientsPlateTypeIndexWrapper ingredientsPlateTypeIndexWrapper;

    @Resource
    private AdminService adminService;
    //private GoogleOCRDetectionServiceImpl ocrDetection;

    @Resource
    private GoogleOCRDetectionService googleOCRDetectionService;

    @Autowired
    private GoogleSheetService googleSheetService;

    @Autowired
    public RecipeIngredientImpl(IngredientPlateTypeCache ingredientPlateTypeCache,
                                IngredientsPlateTypeIndexWrapper ingredientsPlateTypeIndexWrapper
    ) throws IOException {
        this.ingredientPlateTypeCache = ingredientPlateTypeCache;
        this.ingredientsPlateTypeIndexWrapper = ingredientsPlateTypeIndexWrapper;
        // this.ocrDetection = ocrDetection;
        //initLuceneIndexes();
    }

    @Override
    public void resetIngredientList() throws GeneralSecurityException, IOException {
        googleSheetService.resetIngredientList();
    }

    @Override
    public Set<String> addIngredientToSheet(RecipeEntity recipeEntity) throws IOException, GeneralSecurityException {
        Set<String> ingredientList = findIngredientsInText(recipeEntity.getAutoDescription(), ingredientPlateTypeCache.getIngredientEntities());
        try {
            googleSheetService.runUpdate(ingredientList, true);
        } catch (GeneralSecurityException e) {
            log.error("Error while trying to login in google", e);
            throw e;
        } catch (IOException e) {
            throw e;
        }
        return ingredientList;
    }


    @Override
    public String findNameAlgo2(String text) throws IOException {
        String[] textSplitted = text.split("\\n");
        String result = "";
        int i = 0;
        int countWord = 0;
        int lineWithIngredientFoundIndex = Integer.MIN_VALUE;
        for (String line : textSplitted) {

            String[] words = line.split(" ");

            //if first line with an ingredient or plate name not found find it
            if (lineWithIngredientFoundIndex == Integer.MIN_VALUE) {
                for (String word : words) {
                    if (ingredientsPlateTypeIndexWrapper.queryByName(LuceneIndexType.INGREDIENT, word) != null
                            || ingredientsPlateTypeIndexWrapper.queryByName(LuceneIndexType.PLATE_TYPE, word) != null) {
                        lineWithIngredientFoundIndex = i;
                        break;
                    }
                }
            }
            //add line to result of recipe element found and not too much lines
            if (lineWithIngredientFoundIndex >= 0 && (i - lineWithIngredientFoundIndex) < 4) {
                result = result + line + " ";
                countWord += words.length;
            }

            //break condition if enough word or lines parses following first line with ingredient
            if ((i == lineWithIngredientFoundIndex && countWord >= RECIPE_NAME_FIND_ALGO_WORD_COUNT_ON_FIRST_LINE) ||
                    (countWord >= RECIPE_NAME_FIND_ALGO_WORD_COUNT_FROM_FIRST_LINE || (i - lineWithIngredientFoundIndex) > 4)) {
                break;
            }

            i++;
        }

        return StringUtils.capitalize(result.toLowerCase());

    }

    @Override
    public String findNameAlgo2(RecipeEntity recipeEntity) throws IOException {
        return findNameAlgo2(recipeEntity.getAutoDescription());
    }


    @Override
    public Set<String> findIngredientsInText(String sourceText, List<SimpleEntity> matchList) throws IOException {

        if(sourceText==null) return new HashSet<>();

        log.info("Searching elements (ingredient, plate type...) in text with size: " + sourceText.length());
        IndexSearcher searcher = initIndex(sourceText);

        Set<String> ingredientsFound = new HashSet<>();

        //Create a query
        for (SimpleEntity ingredient : matchList) {
            Term term = new Term("description", ingredient.getName().toLowerCase().trim());
            FuzzyQuery query = new FuzzyQuery(term, 1);
            TopDocs results = searcher.search(query, 15);
            //log.info("Searching: " + term.text());

            for (ScoreDoc result : results.scoreDocs) {
                // Document resultDoc = searcher.doc(result.doc);
                log.debug("Match found with score: " + result.score + " for element name: " + term.text());
                ingredientsFound.add(term.text());
            }
        }
        return ingredientsFound;
    }

    private IndexSearcher initIndex(String sourceText) throws IOException {

        IndexSearcher searcher;
        Analyzer analyzer;
        IndexReader reader;
        IndexWriter writer;

        //if (searcher == null) {
        Directory ingredientDir = new RAMDirectory();
        analyzer = new StandardAnalyzer();
        IndexWriterConfig ingredientConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(ingredientDir, ingredientConfig);
        //writer.commit();
        //writer.close();

        //Open an IndexSearcher
        //}

        Document document = new Document();
        document.add(new TextField("description", sourceText.toLowerCase(), Field.Store.YES));
        writer.addDocument(document);

        writer.close();

        reader = DirectoryReader.open(ingredientDir);
        searcher = new IndexSearcher(reader);

        return searcher;
    }


}
