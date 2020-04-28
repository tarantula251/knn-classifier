package metrics;

import logic.Article;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.util.*;

public class EuclideanMetric {

    public HashMap<Article, Double> measureDistance(Article testArticle, ArrayList<Article> masterArticles) {
        DistanceMeasure euclideanMetric = new EuclideanDistance();
        HashMap<Article, Double> masterArticleDistanceMap = new HashMap<Article, Double>();
        double[] testFeatures = testArticle.getFeatures();
        for (Article masterArticle : masterArticles) {
            double[] masterFeatures = masterArticle.getFeatures();
            double distance = euclideanMetric.compute(testFeatures, masterFeatures);
            masterArticleDistanceMap.put(masterArticle, distance);
        }
        return masterArticleDistanceMap;
    }
}
