package logic;

import java.util.ArrayList;

public class Article {
    private String originalBody;
    private ArrayList<String> places;
    private ArrayList<String> bodyTokens;

    public Article() {}

    public Article(String originalBody, ArrayList<String> places, ArrayList<String> bodyTokens) {
        this.originalBody = originalBody;
        this.places = places;
        this.bodyTokens = bodyTokens;
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
}
