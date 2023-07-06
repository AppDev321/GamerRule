package com.gamerrule.android.classes;

import java.util.Date;

public class Transaction {
    private String transactionId;
    private boolean addMoney;
    private Date transactionTime;
    private int transactionAmount;
    private String transactionUser;

    // Empty constructor required for Firestore serialization
    public Transaction() {
    }

    public Transaction(String transactionId, boolean addMoney, Date transactionTime, int transactionAmount, String transactionUser) {
        this.transactionId = transactionId;
        this.addMoney = addMoney;
        this.transactionTime = transactionTime;
        this.transactionAmount = transactionAmount;
        this.transactionUser = transactionUser;
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
}

