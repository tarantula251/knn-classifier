package logic;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class TextAnalyzer {
    final private static CharArraySet stopWords = new CharArraySet(Arrays.asList("a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"), false);
    final private static Path dataDir = Paths.get(Paths.get("").toAbsolutePath().toString() + "\\data_txt");
    final private static File dataDirAsFile = new File(String.valueOf(dataDir));
    private ArrayList<File> dataFiles;
    private static final String BODY_END = "reuter &#3;";
    private static final String SEMICOLON_CHAR = ";";

    public ArrayList<Article> readBody() throws IOException {
        System.out.println("Started reading files.");
        if (dataDirAsFile != null && dataDirAsFile.isDirectory()) {
            dataFiles = new ArrayList<>();
            for (final File fileEntry : dataDirAsFile.listFiles()) {
                if (fileEntry != null) {
                    dataFiles.add(fileEntry);
                }
            }
        }
        ArrayList<Article> articlesCollection = new ArrayList<>();
        if (dataFiles != null && !dataFiles.isEmpty()) {
            System.out.println("Stemming in progress...");
            int articleCounter = 1;
            for (File dataFile : dataFiles) {
                Scanner scanner = new Scanner(dataFile);
                Article article = new Article();
                while (scanner.hasNextLine()) {
                    int articleId = articleCounter;
                    String placeLine = scanner.nextLine();
                    String emptyLine = scanner.nextLine();
                    String bodyLine = scanner.nextLine();

                    article.setId(articleId);

                    article.setOriginalBody(bodyLine);

                    ArrayList<String> placesList = new ArrayList<>(Arrays.asList(placeLine.split(SEMICOLON_CHAR)));
                    article.setPlaces(placesList);

                    if (bodyLine.toLowerCase().endsWith(BODY_END)) {
                        bodyLine = bodyLine.substring(0, bodyLine.toLowerCase().lastIndexOf(BODY_END)).replaceAll("'", "").replaceAll(",", ".");
                        ArrayList<String> stemmedTokens = stem(bodyLine);
                        article.setBodyTokens(stemmedTokens);
                    }
                    break;
                }
                articlesCollection.add(article);
                articleCounter++;
            }
        }
        System.out.println("Successfully read " + articlesCollection.size() + " articles.");
        return articlesCollection;
    }

    private ArrayList<String> stem(String inputText) throws IOException {
        //TODO make the TokenStream case sensitive if it's possible
        ArrayList<String> stemmedTokens = new ArrayList<>();
        StandardAnalyzer stdAnalyzer = new StandardAnalyzer(stopWords);
        TokenStream tokenStream = stdAnalyzer.tokenStream("contents", new StringReader(inputText));
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String term = charTermAttribute.toString();
            if (term != null && !term.isBlank()) {
                stemmedTokens.add(term);
            }
        }
        tokenStream.end();
        tokenStream.close();
        return stemmedTokens;
    }
}
