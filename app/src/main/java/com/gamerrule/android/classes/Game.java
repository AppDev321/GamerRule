package com.gamerrule.android.classes;
public class Game {
    private String documentId;
    private String imageURL;
    private String gameName;
    private String gameDescription;

    public Game() {
        // Default constructor required for Firestore
    }

    public Game(String imageURL, String gameName, String gameDescription) {
        this.imageURL = imageURL;
        this.gameName = gameName;
        this.gameDescription = gameDescription;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameDescription() {
        return gameDescription;
    }

    public void setGameDescription(String gameDescription) {
        this.gameDescription = gameDescription;
    }
}
