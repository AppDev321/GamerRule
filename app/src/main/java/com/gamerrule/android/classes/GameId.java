package com.gamerrule.android.classes;

public class GameId {
    private String documentId;
    private String gameId;
    private String userId;
    private String gameName;

    public GameId() {
        // Empty constructor needed for Firestore deserialization
    }

    public GameId(String documentId, String gameId, String userId, String gameName) {
        this.documentId = documentId;
        this.gameId = gameId;
        this.userId = userId;
        this.gameName = gameName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
