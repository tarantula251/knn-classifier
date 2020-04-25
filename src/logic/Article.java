package logic;

import java.util.ArrayList;

public class Article {
    public int id;
    private String originalBody;
    private ArrayList<String> places;
    private ArrayList<String> bodyTokens;
    private double[] features;
    private ArrayList<String> knnEuclideanPlaces;
    private ArrayList<String> knnManhattanPlaces;
    private ArrayList<String> knnChebyshevPlaces;
    private ArrayList<String> knnCanberraPlaces;
    private ArrayList<String> knnCorrelationPlaces;

    public Article() {}

    public Article(String originalBody, ArrayList<String> places, ArrayList<String> bodyTokens) {
        this.originalBody = originalBody;
        this.places = places;
        this.bodyTokens = bodyTokens;
    }

    public void setId(int articleId) {
        this.id = articleId;
    }

    public int getArticleId() {
        return this.id;
    }

    public void setOriginalBody(String originalBody) {
        this.originalBody = originalBody;
    }

    public String getOriginalBody() {
        return this.originalBody;
    }

    public void setPlaces(ArrayList<String> places) {
        this.places = places;
    }

    public ArrayList<String> getPlaces() {
        return this.places;
    }

    public void setBodyTokens(ArrayList<String> bodyTokens) {
        this.bodyTokens = bodyTokens;
    }

    public ArrayList<String> getBodyTokens() {
        return this.bodyTokens;
    }

    public void setFeatures(double[] features) {
        this.features = features;
    }

    public double[] getFeatures() {
        return this.features;
    }

    public void setKnnEuclideanPlaces(ArrayList<String> knnEuclideanPlaces) {
        this.knnEuclideanPlaces = knnEuclideanPlaces;
    }

    public ArrayList<String> getKnnEuclideanPlaces() {
        return this.knnEuclideanPlaces;
    }

    public void setKnnManhattanPlaces(ArrayList<String> knnManhattanPlaces) {
        this.knnManhattanPlaces = knnManhattanPlaces;
    }

    public ArrayList<String> getKnnManhattanPlaces() {
        return this.knnManhattanPlaces;
    }

    public void setKnnChebyshevPlaces(ArrayList<String> knnChebyshevPlaces) {
        this.knnChebyshevPlaces = knnChebyshevPlaces;
    }

    public ArrayList<String> getKnnChebyshevPlaces() {
        return this.knnChebyshevPlaces;
    }

    public void setKnnCanberraPlaces(ArrayList<String> knnCanberraPlaces) {
        this.knnCanberraPlaces = knnCanberraPlaces;
    }

    public ArrayList<String> getKnnCanberraPlaces() {
        return this.knnCanberraPlaces;
    }

    public void setKnnCorrelationPlaces(ArrayList<String> knnCorrelationPlaces) {
        this.knnCorrelationPlaces = knnCorrelationPlaces;
    }

    public ArrayList<String> getKnnCorrelationPlaces() {
        return this.knnCorrelationPlaces;
    }

    public double[] getFeaturesByIndices(int[] indexArray) {
        double[] featuresArray = new double[indexArray.length];
        int counter = 0;
        for (int index : indexArray) {
            featuresArray[counter] = this.features[index];
            counter++;
        }
        return featuresArray;
    }
}
