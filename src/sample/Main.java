package sample;

import javafx.application.Application;
import javafx.scene.shape.ArcTo;
import javafx.stage.Stage;
import logic.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main extends Application {
    // available features
    private final static String SHORT_COUNT_KEY = "short";
    private final static String MEDIUM_COUNT_KEY = "medium";
    private final static String LONG_COUNT_KEY = "long";
    private final static String AVERAGE_LENGTH_KEY = "average";
    private final static String NUMERICAL_COUNT_KEY = "numerical";
    private final static String KEYWORDS_ALL_COUNT_KEY = "keywordsAll";
    private final static String KEYWORDS_FIRST_HALF_COUNT_KEY = "keywordsFirstHalf";
    private final static String KEYWORDS_DENSITY_KEY = "keywordsDensity";
    // user input for knn classifier, TODO implement it in GUI
    private static int K_NEIGHBOURS_COUNT = 3;
    private static double MASTER_DATASET_DELIMITER = 0.6;
    private static ArrayList<String> KNN_SELECTED_FEATURES;
    private static String KNN_SELECTED_METRIC = Utils.KNN_METRIC_EUCLIDEAN;

    @Override
    public void start(Stage primaryStage) throws Exception{
        // GUI
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 300, 275));
//        primaryStage.show();

        // convert sgm files to txt and filter them
        String projectDir = Paths.get("").toAbsolutePath().toString();
        Path outputDir = Paths.get(projectDir + "\\data_txt").toAbsolutePath();
        ArticleReader.checkIfDirExists(outputDir);
        ArticleReader reader = new ArticleReader(Paths.get(projectDir + "\\data_sgm").toAbsolutePath(), outputDir);
        reader.extract();

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

        // knn classification
        KNN_SELECTED_FEATURES = new ArrayList<String>(10);
        KNN_SELECTED_FEATURES.add(SHORT_COUNT_KEY);
        KNN_SELECTED_FEATURES.add(KEYWORDS_DENSITY_KEY);
        if (!KNN_SELECTED_FEATURES.isEmpty()) {
            KnnClassifier knnClassifier = new KnnClassifier(K_NEIGHBOURS_COUNT, MASTER_DATASET_DELIMITER, KNN_SELECTED_FEATURES, KNN_SELECTED_METRIC, articlesCollection);
            knnClassifier.classify();
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
