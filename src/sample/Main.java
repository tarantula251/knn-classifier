package sample;

import javafx.application.Application;
import javafx.stage.Stage;
import logic.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main extends Application {

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

        // sort article => TF IDF map
        articleTfidfMap = Utils.sortArticleTfidfMapDesc(articleTfidfMap);

        // generate keywords for each article in collection

        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
