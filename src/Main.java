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
        int kNeighboursCount = 5;
        double masterDatasetDelimiter = 0.6;
        ArrayList<String> articleFeatures = new ArrayList<String>(Arrays.asList(
                Utils.TOKENS, Utils.UNIQUE_TOKENS, Utils.SHORT_TOKENS, Utils.MEDIUM_TOKENS, Utils.LONG_TOKENS,
                Utils.AVERAGE_LENGTH, Utils.NUMERICAL_TOKENS, Utils.KEYWORDS, Utils.KEYWORDS_FIRST_HALF, Utils.KEYWORDS_DENSITY
        ));
        System.out.println("Experiment nr 1 began.");
        // experiment nr 1 - classify articles for 10 different k values
        // knn classification for k = 5;
        KnnClassifier knnClassifier = new KnnClassifier(articlesCollection, PROJECT_DIRECTORY);
        knnClassifier.setKnnParameters(kNeighboursCount, masterDatasetDelimiter, articleFeatures, Utils.KNN_METRIC_EUCLIDEAN);
        knnClassifier.classify();
        performExperiment1(knnClassifier, articleFeatures);

        // experiment nr 2 - classify articles for 5 different masterDatasetDelimiter values
        performExperiment2(knnClassifier, articleFeatures);

        // experiment nr 3 - classify articles for 5 different metric values
        performExperiment3(knnClassifier, articleFeatures);

        // experiment nr 4 - classify articles for 5 different sets of features
        performExperiment4(knnClassifier);
    }

    private static void performExperiment1(KnnClassifier knnClassifier, ArrayList<String> articleFeatures) {
        int kNeighboursCount = 7;
        double masterDatasetDelimiter = 0.6;
        for (int counter = 1; counter < 10; counter++) {
            if (kNeighboursCount == 9 || kNeighboursCount == 10 || kNeighboursCount == 11 || kNeighboursCount == 12) {
                kNeighboursCount--;
            }
            if (kNeighboursCount == 17 || kNeighboursCount == 20) {
                kNeighboursCount++;
            }
            knnClassifier.setKnnParameters(kNeighboursCount, masterDatasetDelimiter, articleFeatures, Utils.KNN_METRIC_EUCLIDEAN);
            knnClassifier.classify();
            kNeighboursCount += 2;
        }
        System.out.println("Experiment nr 1 finished!");
    }

    private static void performExperiment2(KnnClassifier knnClassifier, ArrayList<String> articleFeatures) {
        System.out.println("Experiment nr 2 began.");
        int kNeighboursCount = 9;
        double masterDatasetDelimiter = 0.95;
        for (int counter = 0; counter < 5; counter++) {
            knnClassifier.setKnnParameters(kNeighboursCount, masterDatasetDelimiter, articleFeatures, Utils.KNN_METRIC_CANBERRA);
            knnClassifier.classify();
            masterDatasetDelimiter -= 0.1;
        }
        System.out.println("Experiment nr 2 finished!");
    }

    private static void performExperiment3(KnnClassifier knnClassifier, ArrayList<String> articleFeatures) {
        System.out.println("Experiment nr 3 began.");
        int kNeighboursCount = 9;
        double masterDatasetDelimiter = 0.65;
        HashMap<Integer, String> counterMetricMap = new HashMap<>();
        counterMetricMap.put(0, Utils.KNN_METRIC_EUCLIDEAN);
        counterMetricMap.put(1, Utils.KNN_METRIC_MANHATTAN);
        counterMetricMap.put(2, Utils.KNN_METRIC_CHEBYSHEV);
        counterMetricMap.put(3, Utils.KNN_METRIC_CANBERRA);
        counterMetricMap.put(4, Utils.KNN_METRIC_CORRELATION_COEFFICIENT);
        for (int counter = 0; counter < 5; counter++) {
            knnClassifier.setKnnParameters(kNeighboursCount, masterDatasetDelimiter, articleFeatures, counterMetricMap.get(counter));
            knnClassifier.classify();
        }
        System.out.println("Experiment nr 3 finished!");
    }

    private static void performExperiment4(KnnClassifier knnClassifier) {
        System.out.println("Experiment nr 4 began.");
        int kNeighboursCount = 9;
        double masterDatasetDelimiter = 0.65;
        HashMap<Integer, ArrayList<String>> counterFeaturesMap = new HashMap<>();
        counterFeaturesMap.put(0, new ArrayList<String>(Arrays.asList(Utils.TOKENS, Utils.SHORT_TOKENS, Utils.KEYWORDS_DENSITY)));
        counterFeaturesMap.put(1, new ArrayList<String>(Arrays.asList(Utils.UNIQUE_TOKENS, Utils.AVERAGE_LENGTH, Utils.NUMERICAL_TOKENS)));
        counterFeaturesMap.put(2, new ArrayList<String>(Arrays.asList(Utils.UNIQUE_TOKENS, Utils.LONG_TOKENS, Utils.KEYWORDS_FIRST_HALF)));
        counterFeaturesMap.put(3, new ArrayList<String>(Arrays.asList(Utils.MEDIUM_TOKENS, Utils.NUMERICAL_TOKENS, Utils.KEYWORDS)));
        for (int counter = 0; counter < 4; counter++) {
            knnClassifier.setKnnParameters(kNeighboursCount, masterDatasetDelimiter, counterFeaturesMap.get(counter), Utils.KNN_METRIC_CHEBYSHEV);
            knnClassifier.classify();
        }
        System.out.println("Experiment nr 4 finished!");
    }
}
