package logic;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

public class KnnClassifier {
    private final int k;
    private int[] selectedFeaturesIndices;
    private final String metricName;
    private final ArrayList<Article> articlesCollection;
    private ArrayList<Article> masterArticles;
    private ArrayList<Article> testArticles;

    public KnnClassifier(int k, double masterDatasetDelimiter, ArrayList<String> selectedFeatures, String metricName, ArrayList<Article> articlesCollection) {
        this.k = k;
        this.selectedFeaturesIndices = Utils.getTokensIndices(selectedFeatures);
        this.metricName = metricName;
        this.articlesCollection = articlesCollection;
        splitArticles(masterDatasetDelimiter);
    }

    private void splitArticles(double masterDatasetDelimiter) {
        // divide collection to master and test datasets
        Collections.shuffle(articlesCollection);
        int masterArticlesSize = (int) ((double) articlesCollection.size() * masterDatasetDelimiter);
        List<List<Article>> articlesSplit = ListUtils.partition(articlesCollection, masterArticlesSize);
        // save extracted sets of articles
        masterArticles = new ArrayList<>(articlesSplit.get(0));
        testArticles = new ArrayList<>(articlesSplit.get(1));
    }

    public void classify() {
        normalizeFeatures();
        // perform classification
        for (Article testArticle : testArticles) {
            classify(testArticle);
        }
    }

    private void normalizeFeatures() {
        for (Article article : articlesCollection) {
            double[] featuresArray = article.getFeatures();
            double[] normalizedFeaturesArray = calculateNormalizedValues(new DescriptiveStatistics(featuresArray), new NormalDistribution());
            if (normalizedFeaturesArray.length > 0) {
                article.setFeatures(normalizedFeaturesArray);
            }
        }
    }

    private double[] calculateNormalizedValues(DescriptiveStatistics descriptiveStatistics, RealDistribution realDistribution) {
        double[] normalizedFeatures = new double[(int) descriptiveStatistics.getN()];
        double standardDeviation = descriptiveStatistics.getStandardDeviation();
        double mean = descriptiveStatistics.getMean();
        for (int featureIndex = 0; featureIndex < descriptiveStatistics.getN(); ++featureIndex) {
            double zScore = (descriptiveStatistics.getElement(featureIndex) - mean) / standardDeviation;
            double normalizedZScore = 1.0 - realDistribution.cumulativeProbability(Math.abs(zScore));
            normalizedFeatures[featureIndex] = normalizedZScore;
        }
        return normalizedFeatures;
    }

    private void classify(Article testArticle) {
        if (metricName.equals(Utils.KNN_METRIC_EUCLIDEAN)) {
            EuclideanMetric euclideanMetric = new EuclideanMetric(testArticle, masterArticles, k, selectedFeaturesIndices);
            euclideanMetric.compute();
        }
    }
}
