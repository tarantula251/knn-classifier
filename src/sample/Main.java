package sample;

import javafx.application.Application;
import javafx.stage.Stage;
import logic.ArticleReader;
import logic.TextAnalyzer;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        // stem text
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        textAnalyzer.readBody();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
