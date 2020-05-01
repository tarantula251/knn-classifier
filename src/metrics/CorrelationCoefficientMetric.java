package metrics;

import logic.Article;
import logic.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class CorrelationCoefficientMetric {

    public HashMap<Article, Double> measureDistance(Article testArticle, ArrayList<Article> masterArticles, int[] selectedFeaturesIndices) {
        HashMap<Article, Double> masterArticleDistanceMap = new HashMap<Article, Double>();
        double[] testFeatures = testArticle.getFeaturesByIndices(selectedFeaturesIndices);
        for (Article masterArticle : masterArticles) {
            double[] masterFeatures = masterArticle.getFeaturesByIndices(selectedFeaturesIndices);
            double distance = calculateCorrelationCoefficient(testFeatures, masterFeatures);
            masterArticleDistanceMap.put(masterArticle, distance);
        }
        return masterArticleDistanceMap;
    }

    private double calculateCorrelationCoefficient(double[] testFeatures, double[] masterFeatures) {
        double coefficient = 0;
        if (testFeatures.length == masterFeatures.length) {
            double testFeaturesAvg = Utils.calculateMean(testFeatures);
            double masterFeaturesAvg = Utils.calculateMean(masterFeatures);
            double numeratorSum = 0;
            double denominatorTestSum = 0;
            double denominatorMasterSum = 0;
            for (int index = 0; index < testFeatures.length; index++) {
                double denominatorTestPart = Math.pow(testFeatures[index] - testFeaturesAvg, 2);
                denominatorTestSum += denominatorTestPart;

                double denominatorMasterPart = Math.pow(masterFeatures[index] - masterFeaturesAvg, 2);
                denominatorMasterSum += denominatorMasterPart;

                double numerator = (testFeatures[index] - testFeaturesAvg) * (masterFeatures[index] - masterFeaturesAvg);
                numeratorSum += numerator;
            }
            if (denominatorTestSum * denominatorMasterSum != 0) {
                coefficient = numeratorSum / Math.sqrt(denominatorTestSum * denominatorMasterSum);
            }
        }
        return coefficient;
    }
}
