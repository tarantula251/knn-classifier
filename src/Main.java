import logic.*;
import text_processing.ArticleReader;
import text_processing.TextAnalyzer;
import tokens_processing.FeatureExtractor;
import tokens_processing.TfidfAnalyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private final static String PROJECT_DIRECTORY = Paths.get("").toAbsolutePath().toString();

    public static void main(String[] args) throws Exception {
        readInputFiles();
        ArrayList<Article> articlesCollection = generateArticles();
        performKnnClassification(articlesCollection);

        System.exit(0);
    }

    private static void readInputFiles() throws IOException {
        // convert sgm files to txt and filter them
        Path outputDir = Paths.get(PROJECT_DIRECTORY + "\\data_txt").toAbsolutePath();
        ArticleReader.checkIfDirExists(outputDir);
        ArticleReader reader = new ArticleReader(Paths.get(PROJECT_DIRECTORY + "\\data_sgm").toAbsolutePath(), outputDir);
        reader.extract();
    }

    private static ArrayList<Article> generateArticles() throws Exception {
        // stem text and generate articles collection
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        ArrayList<Article> articlesCollection = textAnalyzer.readBody();
        // calculate TF IDF value for tokens in each article
        TfidfAnalyzer tfidfAnalyzer = new TfidfAnalyzer();
        HashMap<Integer, HashMap<String, Double>> articleTfidfMap = tfidfAnalyzer.analyze(articlesCollection);
        // pick keywords for each article in collection
        HashMap<Integer, ArrayList<String>> articleKeywords = Utils.pickKeywords(articleTfidfMap);
        // set features vector on each article
        FeatureExtractor featureExtractor = new FeatureExtractor();
        featureExtractor.extract(articlesCollection, articleKeywords);
        return articlesCollection;
    }

    private static void performKnnClassification(ArrayList<Article> articlesCollection) {
        int kNeighboursCount = 3;
        double masterDatasetDelimiter = 0.6;
        ArrayList<String> articleFeatures = new ArrayList<String>(Arrays.asList(
                Utils.UNIQUE_TOKENS, Utils.SHORT_TOKENS, Utils.MEDIUM_TOKENS, Utils.LONG_TOKENS,
                Utils.AVERAGE_LENGTH, Utils.NUMERICAL_TOKENS, Utils.KEYWORDS_FIRST_HALF, logic.Utils.KEYWORDS_DENSITY
        ));
        // knn classification for k = 3;
        KnnClassifier knnClassifier = new KnnClassifier(kNeighboursCount, masterDatasetDelimiter, articleFeatures, Utils.KNN_METRIC_CANBERRA, articlesCollection, PROJECT_DIRECTORY);
        knnClassifier.classify();
        // experiment nr 1 - classify articles for 10 different k values
//        for (int counter = 1; counter < 10; counter++) {
//            kNeighboursCount += 2;
//            knnClassifier.setKnnParameters(kNeighboursCount, masterDatasetDelimiter, articleFeatures, logic.Utils.KNN_METRIC_CANBERRA);
//            knnClassifier.classify();
//        }
    }
}
