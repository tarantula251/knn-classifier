package tokens_processing;

import java.util.*;

import logic.Article;
import org.apache.commons.collections4.CollectionUtils;

public class TfidfAnalyzer {

    public HashMap<Integer, HashMap<String, Double>> analyze(ArrayList<Article> articlesCollection) {
        System.out.println("Started TF IDF analyzer.");
        if (articlesCollection != null && !articlesCollection.isEmpty()) {
            System.out.println("TF IDF calculation in progress...");
            HashMap<Integer, HashMap<String, Double>> articleIdTfMap = new HashMap<Integer, HashMap<String, Double>>();
            for (Article article : articlesCollection) {
                HashMap<String, Double> tfMap = calculateTf(article);
                articleIdTfMap.put(article.id, tfMap);
            }
            HashMap<String, Double> idfMap = calculateIdf(articlesCollection, articleIdTfMap);
            HashMap<Integer, Set<String>> articleIdTokensMap = new HashMap<Integer, Set<String>>();
            for (int articleId : articleIdTfMap.keySet()) {
                HashMap<String, Double> tfMapPerArticle = articleIdTfMap.get(articleId);
                articleIdTokensMap.put(articleId, tfMapPerArticle.keySet());
            }
            HashMap<Integer, HashMap<String, Double>> articleTfidfMap = new HashMap<Integer, HashMap<String, Double>>();
            for (int articleId : articleIdTokensMap.keySet()) {
                Set<String> articleTokens = articleIdTokensMap.get(articleId);
                HashMap<String, Double> tokenTfidfPerArticleMap = new HashMap<String, Double>();
                for (String token : articleTokens) {
                    double idfValue = 0;
                    double tfValue = 0;
                    if (idfMap.containsKey(token)) {
                        idfValue = idfMap.get(token);
                    }
                    HashMap<String, Double> tfMap = articleIdTfMap.get(articleId);
                    if (tfMap.containsKey(token)) {
                        tfValue = tfMap.get(token);
                    }
                    double tfidfValue = tfValue * idfValue;
                    tokenTfidfPerArticleMap.put(token, tfidfValue);
                }
                articleTfidfMap.put(articleId, tokenTfidfPerArticleMap);
            }
            System.out.println("Successfully analyzed " + articlesCollection.size() + " articles with TF IDF analyzer.");
            return articleTfidfMap;
        } else {
            System.out.println("Articles collection is empty!");
        }
        return null;
    }

    private HashMap<String, Double> calculateTf(Article article) {
        HashMap<String, Double> tfMap = new HashMap<String, Double>();
        ArrayList<String> bodyTokens = article.getBodyTokens();
        if (bodyTokens != null && !bodyTokens.isEmpty()) {
            Map<String, Integer> occurrencesMap = CollectionUtils.getCardinalityMap(bodyTokens);
            for (final Map.Entry<String, Integer> entry: occurrencesMap.entrySet()) {
                double tfValue = (double) entry.getValue() / bodyTokens.size();
                tfMap.put(entry.getKey(), tfValue);
            }
        } else {
            System.out.println("Body tokens in article are empty!");
        }
        return tfMap;
    }

    private HashMap<String, Double> calculateIdf(ArrayList<Article> articlesCollection, HashMap<Integer, HashMap<String, Double>> articleIdTfMap) {
        HashMap<String, Double> idfMap = new HashMap<String, Double>();
        int articlesCount = articlesCollection.size();
        HashSet<String> tokensCollection = new HashSet<String>();
        for (HashMap<String, Double> tfMap : articleIdTfMap.values()) {
            tokensCollection.addAll(tfMap.keySet());
        }
        if (!tokensCollection.isEmpty()) {
            for (String token : tokensCollection) {
                int articlesContainingWordCount = 0;
                for (HashMap<String, Double> tf : articleIdTfMap.values()) {
                    if (tf.containsKey(token) && tf.get(token) > 0) {
                        articlesContainingWordCount++;
                    }
                }
                idfMap.put(token, Math.log10((double) articlesCount / articlesContainingWordCount));
            }
        }
        return idfMap;
    }
}
