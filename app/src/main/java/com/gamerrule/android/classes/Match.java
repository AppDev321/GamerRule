package com.gamerrule.android.classes;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Match implements Serializable {
    private String documentId;
    private List<Participant> participants;

    public String getDocumentId() {
        return documentId;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public Date getMatchSchedule() {
        return matchSchedule;
    }

    public void setMatchSchedule(Date matchSchedule) {
        this.matchSchedule = matchSchedule;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getPrizePool() {
        return prizePool;
    }

    public void setPrizePool(String prizePool) {
        this.prizePool = prizePool;
    }

    public String getPerKill() {
        return perKill;
    }

    public void setPerKill(String perKill) {
        this.perKill = perKill;
    }

    public String getEntryFees() {
        return entryFees;
    }

    public void setEntryFees(String entryFees) {
        this.entryFees = entryFees;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomPasskey() {
        return roomPasskey;
    }

    public void setRoomPasskey(String roomPasskey) {
        this.roomPasskey = roomPasskey;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    private String imageUrl;
    private String gameType;
    private Date matchSchedule;
    private String map;
    private String matchType;
    private String prizePool;
    private String perKill;
    private String entryFees;
    private int maxPlayers;
    private String description;
    private String roomId;
    private String roomPasskey;
    private String matchStatus;

    // Default constructor (required for Firestore deserialization)
    public Match() {
    }

    // Parameterized constructor
    public Match( String imageUrl, String gameType, Date matchSchedule, String map, String matchType,
                 String prizePool, String perKill, String entryFees, int maxPlayers, String description,
                 String roomId, String roomPasskey, String matchStatus) {
        this.imageUrl = imageUrl;
        this.gameType = gameType;
        this.matchSchedule = matchSchedule;
        this.map = map;
        this.matchType = matchType;
        this.prizePool = prizePool;
        this.perKill = perKill;
        this.entryFees = entryFees;
        this.maxPlayers = maxPlayers;
        this.description = description;
        this.roomId = roomId;
        this.roomPasskey = roomPasskey;
        this.matchStatus = matchStatus;
    }

}
