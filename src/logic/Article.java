package logic;

import java.util.ArrayList;

public class Article {
    public int id;
    private String originalBody;
    private ArrayList<String> places;
    private ArrayList<String> bodyTokens;
    private double[] features;
    private ArrayList<String> knnEuclideanPlaces;

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

    public double[] getFeaturesByIndices(int[] indexArray) {
        double[] featuresArray = new double[indexArray.length];
        int counter = 0;
        for (int index : indexArray) {
            featuresArray[counter] = this.features[index];
            counter++;
        }
        return featuresArray;
    }

    public ArrayList<String> getKnnEuclideanPlaces() {
        return this.knnEuclideanPlaces;
    }
}
