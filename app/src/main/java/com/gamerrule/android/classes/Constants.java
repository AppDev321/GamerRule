package com.gamerrule.android.classes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gamerrule.android.ui.users.ViewMatchesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.atomic.AtomicReference;

public class Constants {

    private String TAG;
    private DocumentReference walletRef;
    private double balance;

    public Constants() {
        // Initialize Firestore references
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(FirebaseAuth.getInstance().getUid() != null)
            walletRef = db.collection("wallets").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        TAG = "Constants Class";
    }

    public boolean isUserAdmin () {
        return FirebaseAuth.getInstance().getUid().equals("bpyVWFOrBfhO1QJ3JIse5ax7Iir1") || FirebaseAuth.getInstance().getUid().equals("VNYdPhtjboNBYo41WFr9OO557EQ2");
    }

    public double fetchWalletBalance(Context context) {
        Log.d(TAG, "Fetching wallet balance");

        walletRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        balance=(documentSnapshot.getDouble("balance"));
                    } else {
                        balance=(0.00);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch wallet balance: " + e.getMessage());
                    Toast.makeText(context, "Failed to fetch wallet balance", Toast.LENGTH_SHORT).show();
                });

        return balance;
    }

    public String makeValidPhoneNumber(String phoneNumber) {
        // Remove any non-digit characters from the input phone number
        String digitsOnlyPhoneNumber = phoneNumber.replaceAll("\\D", "");

        // Check if the phone number has a country code
        if (digitsOnlyPhoneNumber.length() > 10) {
            return "+" + digitsOnlyPhoneNumber;
        } else {
            // If the phone number does not have a country code, add "+91" as the country code
            return "+91" + digitsOnlyPhoneNumber;
        }
    }
}
