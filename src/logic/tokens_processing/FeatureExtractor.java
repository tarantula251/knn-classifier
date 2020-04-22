package logic.tokens_processing;

import logic.Article;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FeatureExtractor {
    private final static String SHORT_COUNT_KEY = "short";
    private final static String MEDIUM_COUNT_KEY = "medium";
    private final static String LONG_COUNT_KEY = "long";
    private final static String AVERAGE_LENGTH_KEY = "average";
    private final static String NUMERICAL_COUNT_KEY = "numerical";
    private final static String KEYWORDS_ALL_COUNT_KEY = "keywordsAll";
    private final static String KEYWORDS_FIRST_HALF_COUNT_KEY = "keywordsFirstHalf";
    private final static String KEYWORDS_DENSITY_KEY = "keywordsDensity";
    private final static int SHORT_TOKEN_LENGTH = 3;
    private final static int MEDIUM_TOKEN_LENGTH_UPPER_BOUND = 7;
    private final static double KEYWORDS_FIRST_HALF_TOKENS_DELIMITER = 0.5;
    private final static String NUMERIC_REGEX = "[-+]?\\d+(.?\\d+)?";

    public void extract(ArrayList<Article> articlesCollection, HashMap<Integer, ArrayList<String>> articleKeywords) {
        for (Article article : articlesCollection) {
            ArrayList<String> keywords = articleKeywords.get(article.getArticleId());
            double tokensCount = getTokensCount(article);
            double uniqueTokensCount = getUniqueTokensCount(article);
            HashMap<String, Object> lengthTokensMap = countTokensByLength(article);
            double lengthShortTokensCount = (double) lengthTokensMap.get(SHORT_COUNT_KEY);
            double lengthMediumTokensCount = (double) lengthTokensMap.get(MEDIUM_COUNT_KEY);
            double lengthLongTokensCount = (double) lengthTokensMap.get(LONG_COUNT_KEY);
            double averageTokenLength = (double) lengthTokensMap.get(AVERAGE_LENGTH_KEY);
            double numericalTokensCount = (double) lengthTokensMap.get(NUMERICAL_COUNT_KEY);
            HashMap<String, Object> keywordsOccurencesMap = countTokensByKeywords(article, keywords);
            double keywordsAll = (double) keywordsOccurencesMap.get(KEYWORDS_ALL_COUNT_KEY);
            double keywordsFirstHalf = (double) keywordsOccurencesMap.get(KEYWORDS_FIRST_HALF_COUNT_KEY);
            double keywordsDensity = (double) keywordsOccurencesMap.get(KEYWORDS_DENSITY_KEY);

            ArrayList<Double> featuresList = new ArrayList<Double>();
            featuresList.add(tokensCount);
            featuresList.add(uniqueTokensCount);
            featuresList.add(lengthShortTokensCount);
            featuresList.add(lengthMediumTokensCount);
            featuresList.add(lengthLongTokensCount);
            featuresList.add(averageTokenLength);
            featuresList.add(numericalTokensCount);
            featuresList.add(keywordsAll);
            featuresList.add(keywordsFirstHalf);
            featuresList.add(keywordsDensity);

            double[] featuresArray = featuresList
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            article.setFeatures(featuresArray);
        }
    }

    private int getTokensCount(Article article) {
        return article.getBodyTokens().size();
    }

    private int getUniqueTokensCount(Article article) {
        int uniqueTokensCount = 0;
        ArrayList<String> tokensList = article.getBodyTokens();
        Map<String, Integer> occurrencesMap = CollectionUtils.getCardinalityMap(tokensList);
        for (String token : occurrencesMap.keySet()) {
            if (occurrencesMap.get(token) == 1) {
                uniqueTokensCount++;
            }
        }
        return uniqueTokensCount;
    }

    private HashMap<String, Object> countTokensByLength(Article article) {
        double shortTokensCount = 0;
        double mediumTokensCount = 0;
        double longTokensCount = 0;
        double sumTokensLength = 0;
        double numericalTokensCount = 0;
        ArrayList<String> tokensList = article.getBodyTokens();
        for (String token : tokensList) {
            int length = token.length();
            if (length <= SHORT_TOKEN_LENGTH) {
                shortTokensCount++;
            } else if (length <= MEDIUM_TOKEN_LENGTH_UPPER_BOUND) {
                mediumTokensCount++;
            } else {
                longTokensCount++;
            }
            sumTokensLength += length;
            if (token.matches(NUMERIC_REGEX)) {
                numericalTokensCount++;
            }
        }

        HashMap<String, Object> lengthCountMap = new HashMap<String, Object>();
        lengthCountMap.put(SHORT_COUNT_KEY, shortTokensCount);
        lengthCountMap.put(MEDIUM_COUNT_KEY, mediumTokensCount);
        lengthCountMap.put(LONG_COUNT_KEY, longTokensCount);
        lengthCountMap.put(AVERAGE_LENGTH_KEY, sumTokensLength / tokensList.size());
        lengthCountMap.put(NUMERICAL_COUNT_KEY, numericalTokensCount);
        return lengthCountMap;
    }

    private HashMap<String, Object> countTokensByKeywords(Article article, ArrayList<String> keywords) {
        ArrayList<String> tokensList = article.getBodyTokens();
        double keywordsCountAll = (int) tokensList
                .stream()
                .filter(keywords::contains)
                .count();
        int halfTokensSize = (int) ((double) tokensList.size() * KEYWORDS_FIRST_HALF_TOKENS_DELIMITER);
        ArrayList<String> firstHalfTokensList = tokensList
                .stream()
                .limit(halfTokensSize)
                .collect(Collectors.toCollection(ArrayList::new));
        double keywordsCountFirstHalf = (double) firstHalfTokensList
                .stream()
                .filter(keywords::contains)
                .count();
        double keywordsDensity = keywordsCountAll / tokensList.size();

        HashMap<String, Object> keywordCountMap = new HashMap<String, Object>();
        keywordCountMap.put(KEYWORDS_ALL_COUNT_KEY, keywordsCountAll);
        keywordCountMap.put(KEYWORDS_FIRST_HALF_COUNT_KEY, keywordsCountFirstHalf);
        keywordCountMap.put(KEYWORDS_DENSITY_KEY, keywordsDensity);
        return keywordCountMap;
    }
}
