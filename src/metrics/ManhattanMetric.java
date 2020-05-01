package metrics;

import logic.Article;

import java.util.ArrayList;
import java.util.HashMap;

public class ManhattanMetric {

    public HashMap<Article, Double> measureDistance(Article testArticle, ArrayList<Article> masterArticles, int[] selectedFeaturesIndices) {
        HashMap<Article, Double> masterArticleDistanceMap = new HashMap<Article, Double>();
        double[] testFeatures = testArticle.getFeaturesByIndices(selectedFeaturesIndices);
        for (Article masterArticle : masterArticles) {
            double[] masterFeatures = masterArticle.getFeaturesByIndices(selectedFeaturesIndices);
            double distance = calculateManhattanDistance(testFeatures, masterFeatures);
            masterArticleDistanceMap.put(masterArticle, distance);
        }
        return masterArticleDistanceMap;
    }

    private double calculateManhattanDistance(double[] testFeatures, double[] masterFeatures) {
        double distance = 0;
        if (testFeatures.length == masterFeatures.length) {
            for (int index = 0; index < testFeatures.length; index++) {
                double val = Math.abs(testFeatures[index] - masterFeatures[index]);
                distance += val;
            }
        }
        return distance;
    }
}
