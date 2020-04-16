package logic;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;

public class TfidfAnalyzer {

    public void analyze(ArrayList<Article> articlesCollection) {
        if (articlesCollection != null && !articlesCollection.isEmpty()) {
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
                // TODO extract keywords
                articleTfidfMap.put(articleId, tokenTfidfPerArticleMap);
            }
        } else {
            System.out.println("Articles collection is empty!");
        }
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
