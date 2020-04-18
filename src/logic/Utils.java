package logic;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class Utils {
    private final static double KEYWORDS_LIMIT_PERCENT = 0.6;

    public static HashMap<String, Double> sortTfidfMapDesc(HashMap<String, Double> tfidfMap) {
        HashMap<String, Double> tfidfMapSorted = tfidfMap
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        return tfidfMapSorted;
    }

    public static HashMap<Integer, HashMap<String, Double>> sortArticleTfidfMapDesc(HashMap<Integer, HashMap<String, Double>> articleTfidfMap) {
        if (!articleTfidfMap.isEmpty()) {
            HashMap<Integer, HashMap<String, Double>> articleTfidfMapSorted = new HashMap<Integer, HashMap<String, Double>>();
            for (int articleId : articleTfidfMap.keySet()) {
                HashMap<String, Double> tfidfMap = articleTfidfMap.get(articleId);
                HashMap<String, Double> sortedTfidfMap = sortTfidfMapDesc(tfidfMap);
                articleTfidfMapSorted.put(articleId, sortedTfidfMap);
            }
            return articleTfidfMapSorted;
        }
        return null;
    }

    public static HashMap<Integer, ArrayList<String>> pickKeywords(HashMap<Integer, HashMap<String, Double>> articleTfidfMap) {
        HashMap<Integer, ArrayList<String>> articleKeywordsMap = new HashMap<Integer, ArrayList<String>>();
        if (!articleTfidfMap.isEmpty()) {
            articleTfidfMap = sortArticleTfidfMapDesc(articleTfidfMap);
            for (int articleId : articleTfidfMap.keySet()) {
                HashMap<String, Double> tfidfMap = articleTfidfMap.get(articleId);
                int newTfidfSize = (int) ((double) tfidfMap.size() * KEYWORDS_LIMIT_PERCENT);
                HashMap<String, Double> limitedTfidfMap = tfidfMap
                        .entrySet()
                        .stream()
                        .limit(newTfidfSize)
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
                ArrayList<String> limitedTokens = new ArrayList<>(limitedTfidfMap.keySet());
                articleKeywordsMap.put(articleId, limitedTokens);
            }
        }
        return articleKeywordsMap;
    }
}
