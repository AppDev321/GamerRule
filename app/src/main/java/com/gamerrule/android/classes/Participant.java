package com.gamerrule.android.classes;

import java.util.Date;

public class Participant {
    private String documentId;
    private String matchId;
    private Date participationDate;
    private double entryFeePaid;
    private String gameUsername;


    private String paticipatedUserId;
    private int rank;
    private int kills;
    private double winningAmount;

    private boolean rewarded;

    public Participant() {
        // Empty constructor needed for Firestore deserialization
    }

    public Participant(String matchId, Date participationDate, String participateduid, double entryFeePaid, String gameUsername, int rank, int kills, double winningAmount) {
        this.matchId = matchId;
        this.participationDate = participationDate;
        this.entryFeePaid = entryFeePaid;
        this.gameUsername = gameUsername;
        this.rank = rank;
        this.kills = kills;
        this.winningAmount = winningAmount;
        this.paticipatedUserId = participateduid;
        this.rewarded = false;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public Date getParticipationDate() {
        return participationDate;
    }

    public void setParticipationDate(Date participationDate) {
        this.participationDate = participationDate;
    }

    public boolean isRewarded() {
        return rewarded;
    }

    public void setRewarded(boolean rewarded) {
        this.rewarded = rewarded;
    }

    public String getPaticipatedUserId() {
        return paticipatedUserId;
    }

    public void setPaticipatedUserId(String paticipatedUserId) {
        this.paticipatedUserId = paticipatedUserId;
    }

    public double getEntryFeePaid() {
        return entryFeePaid;
    }

    public void setEntryFeePaid(double entryFeePaid) {
        this.entryFeePaid = entryFeePaid;
    }

    public String getGameUsername() {
        return gameUsername;
    }

    public void setGameUsername(String gameUsername) {
        this.gameUsername = gameUsername;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public double getWinningAmount() {
        return winningAmount;
    }

    public void setWinningAmount(double winningAmount) {
        this.winningAmount = winningAmount;
    }
}

