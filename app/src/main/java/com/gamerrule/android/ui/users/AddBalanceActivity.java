package com.gamerrule.android.ui.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dev.shreyaspatil.easyupipayment.EasyUpiPayment;
import dev.shreyaspatil.easyupipayment.exception.AppNotFoundException;
import dev.shreyaspatil.easyupipayment.listener.PaymentStatusListener;
import dev.shreyaspatil.easyupipayment.model.PaymentApp;
import dev.shreyaspatil.easyupipayment.model.TransactionDetails;
import dev.shreyaspatil.easyupipayment.model.TransactionStatus;

public class AddBalanceActivity extends AppCompatActivity {

    private TextView currentBalanceTextView;
    private EditText addMoneyEditText;
    private Button addMoneyButton;
    private TextView setAmount100TextView;
    private TextView setAmount500TextView;
    private TextView setAmount1000TextView;

    private String transactionId;
    private EasyUpiPayment easyUpiPayment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance);

        // Initialize UI elements
        currentBalanceTextView = findViewById(R.id.current_balance_add_balance);
        addMoneyEditText = findViewById(R.id.add_money_edit_text);
        addMoneyButton = findViewById(R.id.add_money_button);
        setAmount100TextView = findViewById(R.id.set_amount_100);
        setAmount500TextView = findViewById(R.id.set_amount_500);
        setAmount1000TextView = findViewById(R.id.set_amount_1000);

        // Set click listeners
        addMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = addMoneyEditText.getText().toString();

                // Check if the amount is valid
                if (isValidAmount(amount)) {
                    transactionId = "T" + System.currentTimeMillis();
                    makePayment(amount +".00","paytmqr1u7ajq6uv8@paytm","ROHIT RANJAN","gamerbold deposit", transactionId);
                } else {
                    // Display an error message or perform appropriate action
                    Toast.makeText(AddBalanceActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setAmount100TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAmount(100, setAmount100TextView);
            }
        });

        setAmount500TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAmount(500, setAmount500TextView);
            }
        });

        setAmount1000TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAmount(1000, setAmount1000TextView);
            }
        });

    }

    private void setAmount(int amount, TextView selectedTextView) {
        // Set the amount in the EditText
        addMoneyEditText.setText(String.valueOf(amount));

        // Update the button backgrounds and text colors
        setAmount100TextView.setBackgroundResource(R.drawable.background_square_disabled);
        setAmount100TextView.setTextColor(getResources().getColor(R.color.dark_grey));

        setAmount500TextView.setBackgroundResource(R.drawable.background_square_disabled);
        setAmount500TextView.setTextColor(getResources().getColor(R.color.dark_grey));

        setAmount1000TextView.setBackgroundResource(R.drawable.background_square_disabled);
        setAmount1000TextView.setTextColor(getResources().getColor(R.color.dark_grey));

        selectedTextView.setBackgroundResource(R.drawable.background_square_enabled);
        selectedTextView.setTextColor(getResources().getColor(R.color.primary_red));
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchWalletBalance(FirebaseAuth.getInstance().getUid());
    }


    private void makePayment(String amount, String upi, String name, String desc, String transactionId) {
        try {
            // Create an instance of EasyUpiPayment.Builder
            EasyUpiPayment.Builder builder = new EasyUpiPayment.Builder(this)
                    .setPayeeVpa(upi)
                    .setPayeeName(name)
                    .setPayeeMerchantCode("5999")
                    .setTransactionId(transactionId)
                    .setTransactionRefId(transactionId)
                    .setDescription(desc)
                    .setAmount(amount);

            // Build the EasyUpiPayment instance
            EasyUpiPayment easyUpiPayment = builder.build();

            // Set the payment status listener
            easyUpiPayment.setPaymentStatusListener(new PaymentStatusListener() {
                @Override
                public void onTransactionCompleted(TransactionDetails transactionDetails) {
                    Log.d("Transaction Details", transactionDetails.toString());

                    if (transactionDetails.getTransactionStatus() == TransactionStatus.FAILURE) {
                        Toast.makeText(AddBalanceActivity.this, "Transaction Failed...", Toast.LENGTH_SHORT).show();
                    } else if (transactionDetails.getTransactionStatus() == TransactionStatus.SUBMITTED) {
                        Toast.makeText(AddBalanceActivity.this, "Transaction Not Completed. Any money deducted will be refunded.", Toast.LENGTH_LONG).show();
                    } else if (transactionDetails.getTransactionStatus() == TransactionStatus.SUCCESS) {
                        generateTransaction();
                    }
                }

                @Override
                public void onTransactionCancelled() {
                    Toast.makeText(AddBalanceActivity.this, "Transaction cancelled..", Toast.LENGTH_SHORT).show();
                }
            });

            // Start the payment transaction
            easyUpiPayment.startPayment();
        } catch (AppNotFoundException e) {
            // Handle the case when no UPI app is available on the device
            e.printStackTrace();
            Toast.makeText(this, "No UPI app found on your device", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
            Toast.makeText(this, "Error occurred during payment", Toast.LENGTH_SHORT).show();
        }
    }


    private void generateTransaction() {

        // Create transaction object
        boolean addMoney = true;
        Date transactionTime = new Date();
        int transactionAmount = Integer.parseInt(addMoneyEditText.getText().toString());
        String transactionUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Transaction transaction = new Transaction(transactionId, addMoney, transactionTime, transactionAmount, transactionUser);

        // Save transaction object to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transactions").document(transactionId).set(transaction)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Transaction saved successfully
                        updateWalletBalance(transactionUser, transactionAmount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save transaction
                        Toast.makeText(AddBalanceActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Update the wallet balance of the user
    private void updateWalletBalance(String userId, double transactionAmount) {
        DocumentReference walletRef = FirebaseFirestore.getInstance().collection("wallets").document(userId);

        walletRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, update the balance
                        walletRef.update("balance", FieldValue.increment(transactionAmount))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Balance updated successfully
                                        Toast.makeText(AddBalanceActivity.this, "Balance updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Error updating balance
                                        Toast.makeText(AddBalanceActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Document does not exist, create it
                        Map<String, Object> walletData = new HashMap<>();
                        walletData.put("balance", transactionAmount);

                        walletRef.set(walletData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Wallet document created successfully
                                        Toast.makeText(AddBalanceActivity.this, "Wallet created", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Error creating wallet document
                                        Toast.makeText(AddBalanceActivity.this, "Failed to create wallet", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // Error getting document
                    Toast.makeText(AddBalanceActivity.this, "Failed to retrieve wallet data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // Method to validate the amount
    private boolean isValidAmount(String amount) {
        // Perform your validation logic here
        return !TextUtils.isEmpty(amount) && Double.parseDouble(amount) > 0;
    }

    // Fetch the wallet balance for a user
    private void fetchWalletBalance(String userId) {
        DocumentReference walletRef = FirebaseFirestore.getInstance().collection("wallets").document(userId);

        walletRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Wallet document found
                            double balance = documentSnapshot.getDouble("balance");

                            currentBalanceTextView.setText(balance + "  ");
                        } else {
                            // Wallet document not found
                            currentBalanceTextView.setText(" 0.00 ");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error fetching wallet balance
                        Toast.makeText(AddBalanceActivity.this, "Failed to fetch wallet balance", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}