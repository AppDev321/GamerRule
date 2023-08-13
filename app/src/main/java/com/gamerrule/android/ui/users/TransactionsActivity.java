package com.gamerrule.android.ui.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Constants;
import com.gamerrule.android.classes.Transaction;
import com.gamerrule.android.classes.TransactionAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    private Button addBalnce, withdrawBalance;
    private ImageButton backButton;
    private TextView currentBalanceTextView;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private ProgressDialog progressDialog;
    private Double currentWalletBalane=0.0;
    private String AuthUid;
    private boolean isAdmin;
    private boolean viewAll;
    private boolean viewRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        addBalnce= findViewById(R.id.add_balance_transaction);
        // Find views by their IDs
        backButton = findViewById(R.id.ib_back_transactions);
        currentBalanceTextView = findViewById(R.id.current_balance_transactions);
        recyclerView = findViewById(R.id.recycler_view_transactions);
        withdrawBalance = findViewById(R.id.Withdraw_balance_transaction);

        AuthUid = FirebaseAuth.getInstance().getUid();
        isAdmin = new Constants().isUserAdmin();

        if(getIntent().hasExtra("uid")){
            AuthUid = getIntent().getStringExtra("uid");
            if(getIntent().hasExtra("viewRequest")){
                viewRequest = true;
            }
            if(getIntent().hasExtra("viewAll")){
                viewAll = true;
            }
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the transaction list and adapter
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(transactionList, isAdmin );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch wallet balance
        fetchWalletBalance(AuthUid);

        // Fetch transactions for the logged-in user
        fetchTransactionsForUser(AuthUid);

        withdrawBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWithDrawDialog();
            }
        });


        // Set up your UI and perform any necessary operations
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        addBalnce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TransactionsActivity.this, AddBalanceActivity.class));
            }
        });
    }

    private void showWithDrawDialog() {
        // Create the dialog builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_withdraw, null);
        dialogBuilder.setView(dialogView);

        // Get references to dialog views
        EditText editTextBalance = dialogView.findViewById(R.id.editTextBalanceWithdraw);
        EditText editTextUpi = dialogView.findViewById(R.id.editTextUpiWithdraw);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirmWithdraw);

        // Create the dialog
        AlertDialog dialog = dialogBuilder.create();

        // Set click listener for the confirm button
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the balance input
                String balance = editTextBalance.getText().toString().trim();
                String upi = editTextUpi.getText().toString().trim();
                // Validate the balance input
                if (TextUtils.isEmpty(balance) || TextUtils.isEmpty(upi)) {
                    Toast.makeText(TransactionsActivity.this, "Please enter a balance and UPI", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.parseInt(balance) > currentWalletBalane) {
                    Toast.makeText(TransactionsActivity.this, "Insufficient balance", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert the balance input to an integer
                int transactionAmount = Integer.parseInt(balance);

                // Create a new transaction object
                String transactionId = "T" + System.currentTimeMillis(); // generate a unique transaction ID
                boolean addMoney = false; // set based on your logic
                Date transactionTime = new Date(); // set the current date and time
                String transactionUser = AuthUid; // set the user ID for whom the transaction is being made

                Transaction transaction = new Transaction(transactionId, addMoney, transactionTime, transactionAmount, transactionUser,
                        true, false, false);

                transaction.setUpiID(upi);
                // Set other properties of the transaction object if needed

                showLoadingDialog("Requesting Withdraw...");

                // Add the transaction to the Firestore transaction collection
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("transactions").document(transactionId)
                        .set(transaction)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Transaction document updated successfully
                                // Handle success, if required
                                dismissLoadingDialog();
                                dialog.dismiss();
                                fetchTransactionsForUser(AuthUid);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error occurred while updating the transaction document
                                // Handle the error, if required
                                dismissLoadingDialog();
                                dialog.dismiss();
                                Toast.makeText(TransactionsActivity.this, "Failed to update transaction document", Toast.LENGTH_SHORT).show();
                                Log.e("WITHDRAW DIALOG", "Failed to update transaction document", e);
                            }
                        });

                // Close the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }



    private void fetchWalletBalance(String userId) {
        DocumentReference walletRef = FirebaseFirestore.getInstance().collection("wallets").document(userId);
        walletRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Wallet document found
                            double balance = documentSnapshot.getDouble("balance");
                            currentWalletBalane = balance;
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
                        Toast.makeText(TransactionsActivity.this, "Failed to fetch wallet balance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchTransactionsForUser(String userId) {
        Query query;
        if(isAdmin && viewAll){
            query = db.collection("transactions")
                    .orderBy("transactionTime", Query.Direction.DESCENDING);

        }else if(isAdmin && viewRequest){
            query = db.collection("transactions")
                    .whereEqualTo("pending", true)
                    .orderBy("transactionTime", Query.Direction.DESCENDING);

        }else{
            query = db.collection("transactions")
                    .whereEqualTo("transactionUser", userId)
                    .orderBy("transactionTime", Query.Direction.DESCENDING);

        }
        showLoadingDialog("Fetching Transactions...");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    // Clear the existing list before adding new data
                    transactionList.clear();
                    transactionList.addAll(querySnapshot.toObjects(Transaction.class));
                    Log.d("Transaction list fetch",transactionList.toString());
                    // Notify the adapter that the data has changed
                    adapter.notifyDataSetChanged();

                    dismissLoadingDialog();
                }
            } else {
                dismissLoadingDialog();
                // Handle the error
                String errorMessage = task.getException().getMessage();
                Log.e("FETCH", errorMessage);
                Toast.makeText(this, "Failed to fetch transactions: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingDialog(String text) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(text);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }



}