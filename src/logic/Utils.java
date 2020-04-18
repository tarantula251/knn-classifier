package logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class Utils {

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
}
