package metrics;

import logic.Article;

import java.util.ArrayList;
import java.util.HashMap;

public class CanberraMetric {

    public HashMap<Article, Double> measureDistance(Article testArticle, ArrayList<Article> masterArticles) {
        HashMap<Article, Double> masterArticleDistanceMap = new HashMap<Article, Double>();
        double[] testFeatures = testArticle.getFeatures();
        for (Article masterArticle : masterArticles) {
            double[] masterFeatures = masterArticle.getFeatures();
            double distance = calculateCanberraDistance(testFeatures, masterFeatures);
            masterArticleDistanceMap.put(masterArticle, distance);
        }
        return masterArticleDistanceMap;
    }

    private double calculateCanberraDistance(double[] testFeatures, double[] masterFeatures) {
        double distance = 0;
        if (testFeatures.length == masterFeatures.length) {
            for (int index = 0; index < testFeatures.length; index++) {
                double val = 0;
                double denominator = Math.abs(testFeatures[index]) + Math.abs(masterFeatures[index]);
                if (denominator != 0) {
                    val = Math.abs(testFeatures[index] - masterFeatures[index]) / denominator;
                }
                distance += val;
            }
        }
        return distance;
    }
}
