package sample;

import javafx.application.Application;
import javafx.stage.Stage;
import logic.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main extends Application {
    // for test purpose
    private final static String TOKENS = "Count of all tokens in an article";
    private final static String KEYWORDS = "Count of tokens which are keywords in an article";
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
        KNN_SELECTED_FEATURES.add(TOKENS);
        KNN_SELECTED_FEATURES.add(KEYWORDS);
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
