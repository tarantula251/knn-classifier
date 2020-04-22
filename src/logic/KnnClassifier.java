package logic;

import logic.metrics.CanberraMetric;
import logic.metrics.ChebyshevMetric;
import logic.metrics.EuclideanMetric;
import logic.metrics.ManhattanMetric;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
        // measure distance between each test article and articles in master dataset - according to a selected metric
        HashMap<Article, Double> masterArticleDistanceMap = getMasterArticleDistanceMap(testArticle);
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
                saveClassificationResults(testArticle, classifiedPlaces);
            }
        }
    }

    private void saveClassificationResults(Article testArticle, ArrayList<String> classifiedPlaces) {
        if (metricName.equals(Utils.KNN_METRIC_EUCLIDEAN)) {
            testArticle.setKnnEuclideanPlaces(classifiedPlaces);
        } else if (metricName.equals(Utils.KNN_METRIC_MANHATTAN)) {
            testArticle.setKnnManhattanPlaces(classifiedPlaces);
        }
    }

    private HashMap<Article, Double> getMasterArticleDistanceMap(Article testArticle) {
        HashMap<Article, Double> masterArticleDistanceMap = new HashMap<Article, Double>();
        if (metricName.equals(Utils.KNN_METRIC_EUCLIDEAN)) {
            EuclideanMetric euclideanMetric = new EuclideanMetric();
            masterArticleDistanceMap = euclideanMetric.measureDistance(testArticle, masterArticles, selectedFeaturesIndices);
        } else if (metricName.equals(Utils.KNN_METRIC_MANHATTAN)) {
            ManhattanMetric manhattanMetric = new ManhattanMetric();
            masterArticleDistanceMap = manhattanMetric.measureDistance(testArticle, masterArticles, selectedFeaturesIndices);
        } else if (metricName.equals(Utils.KNN_METRIC_CHEBYSHEV)) {
            ChebyshevMetric chebyshevMetric = new ChebyshevMetric();
            masterArticleDistanceMap = chebyshevMetric.measureDistance(testArticle, masterArticles, selectedFeaturesIndices);
        } else if (metricName.equals(Utils.KNN_METRIC_CANBERRA)) {
            CanberraMetric canberraMetric = new CanberraMetric();
            masterArticleDistanceMap = canberraMetric.measureDistance(testArticle, masterArticles, selectedFeaturesIndices);
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
