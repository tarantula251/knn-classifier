package metrics;

import logic.Article;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

public class ChebyshevMetric {

    public HashMap<Article, Double> measureDistance(Article testArticle, ArrayList<Article> masterArticles, int[] selectedFeaturesIndices) {
        HashMap<Article, Double> masterArticleDistanceMap = new HashMap<Article, Double>();
        double[] testFeatures = testArticle.getFeaturesByIndices(selectedFeaturesIndices);
        for (Article masterArticle : masterArticles) {
            double[] masterFeatures = masterArticle.getFeaturesByIndices(selectedFeaturesIndices);
            double distance = calculateChebyshevDistance(testFeatures, masterFeatures);
            masterArticleDistanceMap.put(masterArticle, distance);
        }
        return masterArticleDistanceMap;
    }

    private double calculateChebyshevDistance(double[] testFeatures, double[] masterFeatures) {
        double[] distance = new double[testFeatures.length];
        if (testFeatures.length == masterFeatures.length) {
            for (int index = 0; index < testFeatures.length; index++) {
                double val = Math.abs(testFeatures[index] - masterFeatures[index]);
                distance[index] = val;
            }
        }
        DoubleSummaryStatistics statistics = Arrays.stream(distance).summaryStatistics();
        return statistics.getMax();
    }
}