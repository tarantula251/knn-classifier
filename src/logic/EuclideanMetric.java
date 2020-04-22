package logic;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class EuclideanMetric {
    private Article testArticle;
    private ArrayList<Article> masterArticles;
    private int k;
    private int[] selectedFeaturesIndices;

    public EuclideanMetric(Article testArticle, ArrayList<Article> masterArticles, int k, int[] selectedFeaturesIndices) {
        this.testArticle = testArticle;
        this.masterArticles = masterArticles;
        this.k = k;
        this.selectedFeaturesIndices = selectedFeaturesIndices;
    }

    public void compute() {
        // measure distance between each test article and articles in master dataset
        HashMap<Article, Double> masterArticleDistanceMap = measureDistance();
        if (!masterArticleDistanceMap.isEmpty()) {
            // get k neighbouring article maps
            HashMap<Article, Double> neighbourDistanceMap = masterArticleDistanceMap
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(k)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                            LinkedHashMap::new));
            ArrayList<String> classifiedPlaces = new ArrayList<String>();
            if (neighbourDistanceMap.size() > 0) {
                classifiedPlaces = getClassifiedPlaces(neighbourDistanceMap.keySet());
            }

            // set classified places on the test article
            if (classifiedPlaces != null && !classifiedPlaces.isEmpty()) {
                testArticle.setKnnEuclideanPlaces(classifiedPlaces);
            }
        }
    }

    private HashMap<Article, Double> measureDistance() {
        DistanceMeasure euclideanMetric = new EuclideanDistance();
        HashMap<Article, Double> masterArticleDistanceMap = new HashMap<Article, Double>();
        double[] testFeatures = testArticle.getFeaturesByIndices(selectedFeaturesIndices);
        for (Article masterArticle : masterArticles) {
            double[] masterFeatures = masterArticle.getFeaturesByIndices(selectedFeaturesIndices);
            double distance = euclideanMetric.compute(testFeatures, masterFeatures);
            masterArticleDistanceMap.put(masterArticle, distance);
        }
        return masterArticleDistanceMap;
    }

    private ArrayList<String> getClassifiedPlaces(Set<Article> neighbourArticlesSet) {
        List<ArrayList<String>> neighbourPlacesList = neighbourArticlesSet
                .stream()
                .map(Article::getPlaces)
                .collect(toList());
        if (!neighbourPlacesList.isEmpty()) {
            Map<ArrayList<String>, Integer> occurrencesMap = CollectionUtils.getCardinalityMap(neighbourPlacesList);
            // get places which occur most frequently and sort them
            Map<ArrayList<String>, Integer> mostFrequentPlacesMap = occurrencesMap
                    .entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(1)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                            LinkedHashMap::new));
            ArrayList<String> mostFrequentPlaces = mostFrequentPlacesMap.keySet()
                    .stream()
                    .distinct()
                    .collect(Collectors.toList())
                    .get(0);
            return mostFrequentPlaces;
        }
        return null;
    }
}
