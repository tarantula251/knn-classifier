package logic;

import metrics.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class KnnClassifier {
    private int k;
    private double masterDatasetDelimiter;
    private int[] selectedFeaturesIndices;
    private String metricName;
    private final ArrayList<Article> articlesCollection;
    private ArrayList<String> selectedFeatures;
    private ArrayList<Article> masterArticles;
    private ArrayList<Article> testArticles;
    private final String projectDirectory;
    private ArrayList<Integer> normalizedArticlesIds;

    private final static String PLACES_PREDICTED_KEY = "Predicted";
    private final static String PLACES_ACTUAL_KEY = "Actual";
    private final static String PLACE_CANADA = "canada";
    private final static String PLACE_FRANCE = "france";
    private final static String PLACE_JAPAN = "japan";
    private final static String PLACE_UK = "uk";
    private final static String PLACE_USA = "usa";
    private final static String PLACE_WEST_GERMANY = "west-germany";
    private final static ArrayList<String> PLACES = new ArrayList<String>(Arrays.asList(PLACE_CANADA, PLACE_FRANCE, PLACE_JAPAN, PLACE_UK, PLACE_USA, PLACE_WEST_GERMANY));


    public KnnClassifier(int k, double masterDatasetDelimiter, ArrayList<String> selectedFeatures, String metricName, ArrayList<Article> articlesCollection, String projectDir) {
        Collections.shuffle(articlesCollection);
        this.articlesCollection = articlesCollection;
        this.projectDirectory = projectDir;
        setKnnParameters(k, masterDatasetDelimiter, selectedFeatures, metricName);
        normalizeFeatures();
    }

    public void setKnnParameters(int k, double masterDatasetDelimiter, ArrayList<String> selectedFeatures, String metricName) {
        this.k = k;
        this.masterDatasetDelimiter = masterDatasetDelimiter;
        splitArticles(masterDatasetDelimiter);
        System.out.println("test arts : "+testArticles.size());
        this.selectedFeatures = selectedFeatures;
        this.selectedFeaturesIndices = Utils.getTokensIndices(selectedFeatures);
        this.metricName = metricName;
    }

    private void splitArticles(double masterDatasetDelimiter) {
        // divide collection to master and test datasets
        int masterArticlesSize = (int) ((double) articlesCollection.size() * masterDatasetDelimiter);
        List<List<Article>> articlesSplit = ListUtils.partition(articlesCollection, masterArticlesSize);
        // save extracted sets of articles
        masterArticles = new ArrayList<>(articlesSplit.get(0));
        testArticles = new ArrayList<>(articlesSplit.get(1));
    }

    public void classify() {
        // perform classification
        for (Article testArticle : testArticles) {
            clearClassificationFields(testArticle);
            classify(testArticle);
        }
        // create statistics
        generateStatistics();
    }

    private void clearClassificationFields(Article testArticle) {
        if (testArticle.getKnnEuclideanPlaces() != null) testArticle.setKnnEuclideanPlaces(null);
        if (testArticle.getKnnManhattanPlaces() != null) testArticle.setKnnManhattanPlaces(null);
        if (testArticle.getKnnChebyshevPlaces() != null) testArticle.setKnnChebyshevPlaces(null);
        if (testArticle.getKnnCanberraPlaces() != null) testArticle.setKnnCanberraPlaces(null);
        if (testArticle.getKnnCorrelationPlaces() != null) testArticle.setKnnCorrelationPlaces(null);
    }

    private void normalizeFeatures() {
        for (Article article : articlesCollection) {
            double[] featuresArray = article.getFeatures();
            double[] normalizedFeaturesArray = calculateNormalizedValues(new DescriptiveStatistics(featuresArray));
            if (normalizedFeaturesArray.length > 0) {
                article.setFeatures(normalizedFeaturesArray);
            }
        }
    }

    private double[] calculateNormalizedValues(DescriptiveStatistics descriptiveStatistics) {
        double[] normalizedFeatures = new double[(int) descriptiveStatistics.getN()];
        double standardDeviation = descriptiveStatistics.getStandardDeviation();
        double mean = 0;
        double[] sortedFeatures = descriptiveStatistics.getSortedValues();
        if (sortedFeatures.length % 2 != 0) {
            mean = sortedFeatures[(sortedFeatures.length - 1) / 2];
        } else {
            mean = (sortedFeatures[(sortedFeatures.length / 2) - 1] + sortedFeatures[sortedFeatures.length / 2]) / 2;
        }
        for (int featureIndex = 0; featureIndex < descriptiveStatistics.getN(); ++featureIndex) {
            double zScore = (descriptiveStatistics.getElement(featureIndex) - mean) / standardDeviation;
            normalizedFeatures[featureIndex] = zScore;
        }
        return normalizedFeatures;
    }

    private void classify(Article testArticle) {
        // measure distance between each test article and articles in master dataset - according to a selected metric
        HashMap<Article, Double> masterArticleDistanceMap = getMasterArticleDistanceMap(testArticle);
        if (!masterArticleDistanceMap.isEmpty()) {
            // get k neighbouring article maps
            HashMap<Article, Double> neighbourDistanceMap = new HashMap<Article, Double>();
            // the higher correlation coefficient is, the more similar both articles are
            if (metricName.equals(Utils.KNN_METRIC_CORRELATION_COEFFICIENT)) {
                neighbourDistanceMap = masterArticleDistanceMap
                        .entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .limit(k)
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
            } else {
                neighbourDistanceMap = masterArticleDistanceMap
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .limit(k)
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
            }
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
        } else if (metricName.equals(Utils.KNN_METRIC_CHEBYSHEV)) {
            testArticle.setKnnChebyshevPlaces(classifiedPlaces);
        } else if (metricName.equals(Utils.KNN_METRIC_CANBERRA)) {
            testArticle.setKnnCanberraPlaces(classifiedPlaces);
        } else if (metricName.equals(Utils.KNN_METRIC_CORRELATION_COEFFICIENT)) {
            testArticle.setKnnCorrelationPlaces(classifiedPlaces);
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
        } else if (metricName.equals(Utils.KNN_METRIC_CORRELATION_COEFFICIENT)) {
            CorrelationCoefficientMetric correlationCoefficientMetric = new CorrelationCoefficientMetric();
            masterArticleDistanceMap = correlationCoefficientMetric.measureDistance(testArticle, masterArticles, selectedFeaturesIndices);
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

    private void generateStatistics() {
        HashMap<Integer, HashMap<String, Double>> articleStatisticsMap = new HashMap<Integer, HashMap<String, Double>>();
        HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap = new HashMap<Integer, HashMap<String, ArrayList<String>>>();
        for (Article testArticle : testArticles) {
            ArrayList<String> predictedPlaces = new ArrayList<>();
            if (metricName.equals(Utils.KNN_METRIC_EUCLIDEAN)) {
                predictedPlaces = testArticle.getKnnEuclideanPlaces();
            } else if (metricName.equals(Utils.KNN_METRIC_MANHATTAN)) {
                predictedPlaces = testArticle.getKnnManhattanPlaces();
            } else if (metricName.equals(Utils.KNN_METRIC_CHEBYSHEV)) {
                predictedPlaces = testArticle.getKnnChebyshevPlaces();
            } else if (metricName.equals(Utils.KNN_METRIC_CANBERRA)) {
                predictedPlaces = testArticle.getKnnCanberraPlaces();
            } else if (metricName.equals(Utils.KNN_METRIC_CORRELATION_COEFFICIENT)) {
                predictedPlaces = testArticle.getKnnCorrelationPlaces();
            }
            HashMap<String, ArrayList<String>> typePlacesMap = new HashMap<String, ArrayList<String>>();
            predictedPlaces.sort(String::compareToIgnoreCase);
            ArrayList<String> actualPlaces = testArticle.getPlaces();
            actualPlaces.sort(String::compareToIgnoreCase);
            typePlacesMap.put(PLACES_PREDICTED_KEY, predictedPlaces);
            typePlacesMap.put(PLACES_ACTUAL_KEY, actualPlaces);
            articleIdPlacesMap.put(testArticle.getArticleId(), typePlacesMap);
        }
        /* Version with only 2 possible classes */
        double accuracyPercentArticles = getAccuracyArticles(articleIdPlacesMap);
        HashMap<String, Double> placePrecisionPercentArticlesMap = getPrecisionArticles(articleIdPlacesMap);
        HashMap<String, Double> placeRecallPercentArticlesMap = getRecallArticles(articleIdPlacesMap);
        /* Version with 6 possible classes (using confusion matrix) */
        int[][] confusionMatrix = createConfusionMatrix(articleIdPlacesMap);
        double accuracyPercent = getAccuracy(confusionMatrix) * 100;
        HashMap<String, Double> placePrecisionPercentMap = getPrecision(confusionMatrix);
        HashMap<String, Double> placeRecallPercentMap = getRecall(confusionMatrix);
        saveExperimentResults(accuracyPercent, placePrecisionPercentMap, placeRecallPercentMap, confusionMatrix, accuracyPercentArticles, placePrecisionPercentArticlesMap, placeRecallPercentArticlesMap);
    }

    private int[][] createConfusionMatrix(HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap) {
        int[][] confusionMatrix = new int[6][6];
        if (!articleIdPlacesMap.isEmpty()) {
            //row - predicted value
            int rowIndex = 0;
            for (String predictedPlace : PLACES) {
                int columnIndex = 0;
                //column - actual value
                for (String actualPlace : PLACES) {
                    confusionMatrix[rowIndex][columnIndex] = countPlacesForConfusionMatrix(articleIdPlacesMap, predictedPlace, actualPlace);
                    columnIndex++;
                }
                rowIndex++;
            }
        }
        return confusionMatrix;
    }

    private int countPlacesForConfusionMatrix(HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap, String predictedPlace, String actualPlace) {
        int placeCounter = 0;
        for (HashMap<String, ArrayList<String>> placesMap : articleIdPlacesMap.values()) {
            ArrayList<String> actualPlaces = placesMap.get(PLACES_ACTUAL_KEY);
            ArrayList<String> predictedPlaces = placesMap.get(PLACES_PREDICTED_KEY);
            if (predictedPlaces.contains(predictedPlace) && actualPlaces.contains(actualPlace)) {
                placeCounter++;
            }
        }
        return placeCounter;
    }

    private double getAccuracy(int[][] confusionMatrix) {
        int matchesCounter = 0;
        int sumMatrixValues = 0;
        for (int rowIndex = 0; rowIndex < 6; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 6; columnIndex++) {
                sumMatrixValues += confusionMatrix[rowIndex][columnIndex];
                if (rowIndex == columnIndex) {
                    matchesCounter += confusionMatrix[rowIndex][columnIndex];
                }
            }
        }
        return (double) matchesCounter / sumMatrixValues;
    }

    private double getAccuracyArticles(HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap) {
        // count: correctly classified articles / size of classification set
        int matchesCounter = 0;
        for (int articleId : articleIdPlacesMap.keySet()) {
            HashMap<String, ArrayList<String>> placesMap = articleIdPlacesMap.get(articleId);
            ArrayList<String> predictedPlaces = placesMap.get(PLACES_PREDICTED_KEY);
            ArrayList<String> actualPlaces = placesMap.get(PLACES_ACTUAL_KEY);
            for (String actualPlace : actualPlaces) {
                if (predictedPlaces.contains(actualPlace)) {
                    matchesCounter++;
                    break;
                }
            }
        }
        return (double) matchesCounter * 100 / articleIdPlacesMap.size();
    }

    private HashMap<String, Double> getPrecision(int[][] confusionMatrix){
        HashMap<String, Double> placePrecisionMap = new HashMap<String, Double>();
        int rowIndex = 0;
        for (String place : PLACES) {
            int rowSumMatrixValues = 0;
            int matchesCounter = 0;
            for (int columnIndex = 0; columnIndex < 6; columnIndex++) {
                rowSumMatrixValues += confusionMatrix[rowIndex][columnIndex];
                if (rowIndex == columnIndex) {
                    matchesCounter = confusionMatrix[rowIndex][columnIndex];
                }
            }
            if (rowSumMatrixValues != 0) {
                placePrecisionMap.put(place, (double) matchesCounter * 100 / rowSumMatrixValues);
            } else {
                placePrecisionMap.put(place, (double) 0);
            }
            rowIndex++;
        }
        placePrecisionMap = placePrecisionMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        return placePrecisionMap;
    }

    private HashMap<String, Double> getPrecisionArticles(HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap) {
        // count: correctly classified articles to place X / count of both correctly and incorrectly classified articles to place X
        HashMap<String, Double> placePrecisionMap = new HashMap<String, Double>();
        for (String place : PLACES) {
            placePrecisionMap.put(place, countPrecision(articleIdPlacesMap, place));
        }
        placePrecisionMap = placePrecisionMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        return placePrecisionMap;
    }

    private double countPrecision(HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap, String placeToCheck) {
        int placeMatchCounter = 0;
        int mismatchCounter = 0;
        for (HashMap<String, ArrayList<String>> placesMap : articleIdPlacesMap.values()) {
            ArrayList<String> actualPlaces = placesMap.get(PLACES_ACTUAL_KEY);
            ArrayList<String> predictedPlaces = placesMap.get(PLACES_PREDICTED_KEY);
            if (actualPlaces.contains(placeToCheck) && predictedPlaces.contains(placeToCheck)) {
                placeMatchCounter++;
            } else if (!actualPlaces.contains(placeToCheck) && predictedPlaces.contains(placeToCheck)) {
                mismatchCounter++;
            }
        }
        if ((placeMatchCounter + mismatchCounter) == 0) return 0;
        return (double) placeMatchCounter * 100 / (placeMatchCounter + mismatchCounter);
    }

    private HashMap<String, Double> getRecall(int[][] confusionMatrix){
        HashMap<String, Double> placeRecallMap = new HashMap<String, Double>();
        int columnIndex = 0;
        for (String place : PLACES) {
            int columnSumMatrixValues = 0;
            int matchesCounter = 0;
            for (int rowIndex = 0; rowIndex < 6; rowIndex++) {
                columnSumMatrixValues += confusionMatrix[rowIndex][columnIndex];
                if (rowIndex == columnIndex) {
                    matchesCounter = confusionMatrix[rowIndex][columnIndex];
                }
            }
            if (columnSumMatrixValues != 0) {
                placeRecallMap.put(place, (double) matchesCounter * 100 / columnSumMatrixValues);
            } else {
                placeRecallMap.put(place, (double) 0);
            }
            columnIndex++;
        }
        placeRecallMap = placeRecallMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        return placeRecallMap;
    }

    private HashMap<String, Double> getRecallArticles(HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap) {
        // count: correctly classified articles to place X / count of articles with place X
        HashMap<String, Double> placeRecallMap = new HashMap<String, Double>();
        for (String place : PLACES) {
            placeRecallMap.put(place, countRecall(articleIdPlacesMap, place));
        }
        placeRecallMap = placeRecallMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        return placeRecallMap;
    }

    private double countRecall(HashMap<Integer, HashMap<String, ArrayList<String>>> articleIdPlacesMap, String placeToCheck) {
        int placeMatchCounter = 0;
        int actualPlaceCounter = 0;
        for (HashMap<String, ArrayList<String>> placesMap : articleIdPlacesMap.values()) {
            ArrayList<String> actualPlaces = placesMap.get(PLACES_ACTUAL_KEY);
            ArrayList<String> predictedPlaces = placesMap.get(PLACES_PREDICTED_KEY);
            if (actualPlaces.contains(placeToCheck)) {
                actualPlaceCounter++;
                if (predictedPlaces.contains(placeToCheck)) {
                    placeMatchCounter++;
                }
            }
        }
        if (actualPlaceCounter == 0) return 0;
        return (double) placeMatchCounter * 100 / actualPlaceCounter;
    }

    private void saveExperimentResults(double accuracyPercent, HashMap<String, Double> placePrecisionPercentMap, HashMap<String, Double> placeRecallPercentMap, int[][] confusionMatrix,
                                       double accuracyPercentArticles, HashMap<String, Double> placePrecisionPercentArticlesMap, HashMap<String, Double> placeRecallPercentArticlesMap) {
        Charset utf8 = StandardCharsets.UTF_8;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(projectDirectory + "\\knn_results\\output_" + System.currentTimeMillis() + ".log"), utf8))) {
            writer.write("*** KNN Algorithm Parameters ***\n");
            writer.write("k: " + this.k + "\n");
            double masterDataPercent = this.masterDatasetDelimiter * 100;
            double testDataPercent = (1 - this.masterDatasetDelimiter) * 100;
            writer.write("dataset division (master / test): " + String.format("%.2f", masterDataPercent) + "% / " + String.format("%.2f", testDataPercent) + "%\n");
            writer.write("test dataset size: " + testArticles.size() + "%\n");
            writer.write("selected metric: " + this.metricName + "\n");
            writer.write("selected features: \n");
            for (String selectedFeature : this.selectedFeatures) {
                writer.write("\t" + selectedFeature + "\n");
            }
            writer.write("\n*** Classification Results ***\n");
            writer.write("*** Confusion matrix regarding all possible classes ***\n");
            writer.write("\t\t\t\t\tCanada\t\t\t\tFrance\t\t\tJapan\t\t\tUK\t\t\t\tUSA\t\t\t\tWest Germany\n");
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (i == 0 && j == 0) {
                        writer.write("Canada\t\t\t\t" + confusionMatrix[i][j] + "\t\t\t");
                    } else if (i == 1 && j == 0) {
                        writer.write("France\t\t\t\t" + confusionMatrix[i][j] + "\t\t\t");
                    } else if (i == 2 && j == 0) {
                        writer.write("Japan\t\t\t\t" + confusionMatrix[i][j] + "\t\t\t");
                    } else if (i == 3 && j == 0) {
                        writer.write("UK\t\t\t\t\t" + confusionMatrix[i][j] + "\t\t\t");
                    } else if (i == 4 && j == 0) {
                        writer.write("USA\t\t\t\t\t" + confusionMatrix[i][j] + "\t\t\t");
                    } else if (i == 5 && j == 0) {
                        writer.write("West Germany\t\t" + confusionMatrix[i][j] + "\t\t\t");
                    } else {
                        writer.write(confusionMatrix[i][j] + "\t\t");
                    }
                }
                writer.write("\n");
            }
            writer.write("\n*** Classification Results Computed On The Basis Of Confusion Matrix ***\n");
            writer.write("accuracy: " + String.format("%.2f", accuracyPercent) + "%\n");
            writer.write("precision: \n");
            for (String place : placePrecisionPercentMap.keySet()) {
                writer.write("\t" + place + " => " + String.format("%.2f", placePrecisionPercentMap.get(place)) + "%\n");
            }
            writer.write("recall: \n");
            for (String place : placeRecallPercentMap.keySet()) {
                writer.write("\t" + place + " => " + String.format("%.2f", placeRecallPercentMap.get(place)) + "%\n");
            }
            writer.write("\n*** Classification Results Computed On The Basis Of Only Two Possible Status - Classified Correctly and Incorrectly ***\n");
            writer.write("accuracy: " + String.format("%.2f", accuracyPercentArticles) + "%\n");
            writer.write("precision: \n");
            for (String place : placePrecisionPercentArticlesMap.keySet()) {
                writer.write("\t" + place + " => " + String.format("%.2f", placePrecisionPercentArticlesMap.get(place)) + "%\n");
            }
            writer.write("recall: \n");
            for (String place : placeRecallPercentArticlesMap.keySet()) {
                writer.write("\t" + place + " => " + String.format("%.2f", placeRecallPercentArticlesMap.get(place)) + "%\n");
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }
}
