package smartrecipe.service.helper;

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
import smartrecipe.service.ocr.GoogleOCRDetection;
import smartrecipe.service.utils.Hash;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class RecipeIngredientHelper {

    private IngredientPlateTypeCache ingredientPlateTypeCache;
    private IngredientsPlateTypeIndexWrapper ingredientsPlateTypeIndexWrapper;

    @Autowired
    public RecipeIngredientHelper(IngredientPlateTypeCache ingredientPlateTypeCache, IngredientsPlateTypeIndexWrapper ingredientsPlateTypeIndexWrapper) throws IOException {
        this.ingredientPlateTypeCache = ingredientPlateTypeCache;
        this.ingredientsPlateTypeIndexWrapper = ingredientsPlateTypeIndexWrapper;
        //init();
    }

    public void resetIngredientList() throws GeneralSecurityException, IOException {
        GoogleSheetHelper sheetsQuickstart = new GoogleSheetHelper();
        sheetsQuickstart.resetIngredientList();
    }

    public Set<String> addIngredientToSheet(RecipeEntity recipeEntity) throws IOException, GeneralSecurityException {
        Set<String> ingredientList = findIngredientsByRecipe(recipeEntity);

        GoogleSheetHelper sheetsQuickstart = new GoogleSheetHelper();
        try {
            sheetsQuickstart.runUpdate(ingredientList,true);
        } catch (GeneralSecurityException e) {
            log.error("Error while trying to login in google", e);
            throw e;
        } catch (IOException e) {
            throw e;
        }
        return ingredientList;
    }


    public void decorateRecipeWithBinaryDescription(RecipeEntity recipe) {
        //get text from image with OCR
        recipe.getRecipeBinaryEntity().setBinaryDescriptionChecksum(Hash.MD5.checksum(recipe.getRecipeBinaryEntity().getBinaryDescription()));
        GoogleOCRDetection ocrDetection = new GoogleOCRDetection();
        String autoDescription = ocrDetection.detect(recipe.getRecipeBinaryEntity().getBinaryDescription());
        recipe.setAutoDescription(autoDescription);
        //if name has not been modified manually, find name from image
        if (recipe.getName() == null || !recipe.isNameModifiedManual()) {
            String autoName = null;
            try {
                autoName = findNameAlgo2(recipe);
            } catch (IOException e) {
                log.error("Error find name from image", e);
            }
            recipe.setName(autoName);
        }
    }

    public String findNameAlgo2(RecipeEntity recipeEntity) throws IOException {
        String[] textSplitted = recipeEntity.getAutoDescription().split("\\n");
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
            if ((i == lineWithIngredientFoundIndex && countWord >= 3) || (countWord >= 5 || (i - lineWithIngredientFoundIndex) > 4)) {
                break;
            }

            i++;
        }

        return StringUtils.capitalize(result.toLowerCase());

    }


    public Set<String> findIngredientsByRecipe(RecipeEntity recipeEntity) throws IOException {

        log.info("Searching ingredients for recipe: " + recipeEntity.getName());
        IndexSearcher searcher = initIndex(recipeEntity);

        Set<String> ingredientsFound = new HashSet<>();

        //Create a query
        for (SimpleEntity ingredient : ingredientPlateTypeCache.getIngredientEntities()) {
            Term term = new Term("description", StringUtils.remove(ingredient.getName().toLowerCase(), " "));
            FuzzyQuery query = new FuzzyQuery(term, 0);
            TopDocs results = searcher.search(query, 15);
            //log.info("Searching: " + term.text());

            for (ScoreDoc result : results.scoreDocs) {
                Document resultDoc = searcher.doc(result.doc);
                log.info("Match found with score: " + result.score + " for ingredient name: " + term.text());
                ingredientsFound.add(term.text());
            }
        }
        return ingredientsFound;
    }

    private IndexSearcher initIndex(RecipeEntity recipeEntity) throws IOException {

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
        document.add(new TextField("description", recipeEntity.getAutoDescription().toLowerCase(), Field.Store.YES));
        writer.addDocument(document);
        Document document2 = new Document();
        document2.add(new TextField("description", "test", Field.Store.YES));
        //writer.addDocument(document2);
        writer.close();

        reader = DirectoryReader.open(ingredientDir);
        searcher = new IndexSearcher(reader);

        return searcher;
    }


}
