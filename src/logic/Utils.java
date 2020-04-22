package logic;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Utils {
    private final static double KEYWORDS_LIMIT_PERCENT = 0.6;
    // knn metric names
    public final static String KNN_METRIC_EUCLIDEAN = "Euclidean";
    public final static String KNN_METRIC_MANHATTAN = "Manhattan";
    public final static String KNN_METRIC_CHEBYSHEV = "Chebyshev";
    // features
    private final static String TOKENS = "Count of all tokens in an article";
    private final static String UNIQUE_TOKENS = "Count of unique tokens in an article";
    private final static String SHORT_TOKENS = "Count of short tokens in an article (length up to 3 chars)";
    private final static String MEDIUM_TOKENS = "Count of medium tokens in an article (length between 4 and 7 chars)";
    private final static String LONG_TOKENS = "Count of long tokens in an article (length greater or equal 8 chars)";
    private final static String AVERAGE_LENGTH = "Average length of tokens in an article";
    private final static String NUMERICAL_TOKENS = "Count of tokens representing a numerical value in an article";
    private final static String KEYWORDS = "Count of tokens which are keywords in an article";
    private final static String KEYWORDS_FIRST_HALF = "Count of tokens which are keywords and appear in the first half of an article";
    private final static String KEYWORDS_DENSITY = "Density of keywords in an article";

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

    public static int[] getTokensIndices(ArrayList<String> selectedFeatures) {
        HashMap<Integer, String> indexFeatureMap = new HashMap<Integer, String>();
        indexFeatureMap.put(0, TOKENS);
        indexFeatureMap.put(1, UNIQUE_TOKENS);
        indexFeatureMap.put(2, SHORT_TOKENS);
        indexFeatureMap.put(3, MEDIUM_TOKENS);
        indexFeatureMap.put(4, LONG_TOKENS);
        indexFeatureMap.put(5, AVERAGE_LENGTH);
        indexFeatureMap.put(6, NUMERICAL_TOKENS);
        indexFeatureMap.put(7, KEYWORDS);
        indexFeatureMap.put(8, KEYWORDS_FIRST_HALF);
        indexFeatureMap.put(9, KEYWORDS_DENSITY);
        int[] indexArray = new int[10];
        int counter = 0;
        for (int featureKey : indexFeatureMap.keySet()) {
            String featureVal = indexFeatureMap.get(featureKey);
            if (selectedFeatures.contains(featureVal)) {
                indexArray[counter] = featureKey;
                counter++;
            }
        }
        return indexArray;
    }
}
