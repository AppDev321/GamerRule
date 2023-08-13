package com.gamerrule.android.classes;

import java.util.Date;

public class Transaction {
    private String transactionId;
    private boolean addMoney;
    private String upiID;
    private boolean pending;
    private boolean game;
    private boolean failed;
    private Date transactionTime;
    private int transactionAmount;
    private String transactionUser;

    // Empty constructor required for Firestore serialization
    public Transaction() {
    }

    public Transaction(String transactionId, boolean addMoney, Date transactionTime, int transactionAmount, String transactionUser, boolean pending, boolean failed, boolean game) {
        this.transactionId = transactionId;
        this.addMoney = addMoney;
        this.transactionTime = transactionTime;
        this.transactionAmount = transactionAmount;
        this.transactionUser = transactionUser;
        this.pending = pending;
        this.game = game;
        this.failed = failed;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isAddMoney() {
        return addMoney;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public int getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionUser() {
        return transactionUser;
    }


    public boolean isGame() {
        return this.game;
    }

    public void setGame(boolean game) {
        this.game = game;
    }


    public String getUpiID() {
        return upiID;
    }

    public void setUpiID(String upiID) {
        this.upiID = upiID;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
