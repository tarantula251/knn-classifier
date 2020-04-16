package sample;

import javafx.application.Application;
import javafx.stage.Stage;
import logic.Article;
import logic.ArticleReader;
import logic.TextAnalyzer;
import logic.TfidfAnalyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
        Path outputDir = Paths.get(projectDir + "\\data_txt");
        ArticleReader.checkIfDirExists(outputDir);
        ArticleReader reader = new ArticleReader(Paths.get(projectDir + "\\data_sgm"), outputDir);
        reader.extract();

        // stem text and generate articles collection
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        ArrayList<Article> articlesCollection = textAnalyzer.readBody();

        // generate keywords for each article in collection
        TfidfAnalyzer tfidfAnalyzer = new TfidfAnalyzer();
        tfidfAnalyzer.analyze(articlesCollection);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
