package smartrecipe.service.helper;


import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smartrecipe.service.entity.SimpleEntity;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class IngredientsPlateTypeIndexWrapper {

    private Analyzer ingredientAnalyzer;
    private IndexReader ingredientReader;
    private IndexSearcher ingredientSearcher;

    private Analyzer plateAnalyzer;
    private IndexReader plateReader;
    private IndexSearcher plateSearcher;

    private IngredientPlateTypeCache ingredientPlateTypeCache;


    @Autowired
    public IngredientsPlateTypeIndexWrapper(IngredientPlateTypeCache ingredientPlateTypeCache) throws IOException, ParseException {
        this.ingredientPlateTypeCache = ingredientPlateTypeCache;
        init();
    }



    private void init() throws IOException, ParseException {

        List<SimpleEntity> ingredientEntities = ingredientPlateTypeCache.getIngredientEntities();
        List<SimpleEntity> plateTypeEntities = ingredientPlateTypeCache.getPlateTypeEntities();

        Directory ingredientDir = new RAMDirectory();
        this.ingredientAnalyzer = new StandardAnalyzer();
        IndexWriterConfig ingredientConfig = new IndexWriterConfig(ingredientAnalyzer);
        IndexWriter ingredientWriter = new IndexWriter(ingredientDir, ingredientConfig);

        Directory plateDir = new RAMDirectory();
        this.plateAnalyzer = new StandardAnalyzer();
        IndexWriterConfig plateConfig = new IndexWriterConfig(plateAnalyzer);
        IndexWriter plateWriter = new IndexWriter(plateDir, plateConfig);


        addDocumentToIndex(ingredientEntities, ingredientWriter);
        addDocumentToIndex(plateTypeEntities, plateWriter);

        //Open an IndexSearcher
        this.ingredientReader = DirectoryReader.open(ingredientDir);
        this.ingredientSearcher = new IndexSearcher(ingredientReader);
        this.plateReader = DirectoryReader.open(plateDir);
        this.plateSearcher = new IndexSearcher(plateReader);

        log.info("Lucene index initialized on ingredients and plates");
    }

    private void addDocumentToIndex(List<SimpleEntity> entities, IndexWriter writer) throws IOException {
        for (SimpleEntity entity : entities) {
            Document document = new Document();
            document.add(new TextField("name", entity.getName().toLowerCase(), Field.Store.YES));
            writer.addDocument(document);
        }
        writer.close();
    }

    public String queryByName(LuceneIndexType indexType, String name) throws IOException {
        //Create a query
        Term term = new Term("name", name.toLowerCase());
        FuzzyQuery query = new FuzzyQuery(term, 1);
        IndexSearcher searcher = null;
        switch (indexType) {
            case INGREDIENT:
                searcher = ingredientSearcher;
                break;
            case PLATE_TYPE:
                searcher = plateSearcher;
                break;
        }
        TopDocs results = searcher.search(query, 10);
        log.debug("Searching by name:" + name);
        String anyDoc = null;
        for (ScoreDoc result : results.scoreDocs) {
            Document resultDoc = searcher.doc(result.doc);
            log.debug("score: " + result.score + " -- text: " + resultDoc.get("name"));
            anyDoc = resultDoc.get("name");
        }

        return anyDoc;
    }

    public void closeReader() throws IOException {
        this.ingredientReader.close();
    }

}
