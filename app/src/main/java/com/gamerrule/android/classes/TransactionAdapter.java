package com.gamerrule.android.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gamerrule.android.R;
import com.gamerrule.android.ui.users.AddBalanceActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private boolean isAdmin;
    private FirebaseFirestore db;

    public TransactionAdapter(List<Transaction> transactions, boolean isAdmin) {
        this.transactionList = transactions;
        this.isAdmin = isAdmin;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.transactionIdTextView.setText(transaction.getTransactionId() + " | " + transaction.getTransactionTime().toLocaleString());


        if (transaction.isAddMoney()) {
            holder.transactionTypeTextView.setText("Deposit");
            holder.amountTextView.setText("+" + transaction.getTransactionAmount());
            int greenColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green);
            holder.amountTextView.setTextColor(greenColor);

            if(transaction.isPending()){
                holder.transactionTypeTextView.setText("Deposit (Pending)");
                holder.amountTextView.setText("+" + transaction.getTransactionAmount());
                int orangeColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.orange);
                holder.amountTextView.setTextColor(orangeColor);

                if (isAdmin) {
                    holder.layoutAdminActionWithdraw.setVisibility(View.VISIBLE);

                    holder.acceptWithdrawRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateTransactionStatus(transaction.getTransactionId(), true, holder);
                        }
                    });

                    holder.declineWithdrawRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateTransactionStatus(transaction.getTransactionId(), false, holder);
                        }
                    });
                }
            }else{
                if (transaction.isFailed()) {
                    holder.transactionTypeTextView.setText("Failed Transaction");
                    holder.amountTextView.setText("-" + transaction.getTransactionAmount());
                    int redColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_red);
                    holder.amountTextView.setTextColor(redColor);
                }
            }
        } else {
            holder.transactionTypeTextView.setText("Withdraw");
            holder.amountTextView.setText("-" + transaction.getTransactionAmount());
            int redColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_red);
            holder.amountTextView.setTextColor(redColor);

            if (transaction.isGame()) {
                holder.transactionTypeTextView.setText("Game");
                holder.amountTextView.setText("-" + transaction.getTransactionAmount());
                holder.amountTextView.setTextColor(redColor);
            }else {
                if (transaction.isPending()) {
                    holder.transactionTypeTextView.setText("Pending Withdraw Request.");
                    holder.amountTextView.setText("-" + transaction.getTransactionAmount());
                    holder.amountTextView.setTextColor(redColor);
                    holder.upiIdWithdrawTextView.setVisibility(View.VISIBLE);
                    holder.upiIdWithdrawTextView.setText("Requested Upi: " + transaction.getUpiID());

                    if (isAdmin) {
                        holder.layoutAdminActionWithdraw.setVisibility(View.VISIBLE);

                        holder.acceptWithdrawRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                updateTransactionStatus(transaction.getTransactionId(), true, holder);
                            }
                        });

                        holder.declineWithdrawRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                updateTransactionStatus(transaction.getTransactionId(), false, holder);
                            }
                        });
                    }
                }else if(transaction.isFailed()) {
                    holder.transactionTypeTextView.setText("Failed Transaction");
                    holder.amountTextView.setText("-" + transaction.getTransactionAmount());
                    holder.amountTextView.setTextColor(redColor);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionIdTextView;
        TextView transactionTypeTextView;
        TextView amountTextView;
        LinearLayout layoutAdminActionWithdraw;
        TextView upiIdWithdrawTextView;
        Button acceptWithdrawRequest, declineWithdrawRequest;

        TransactionViewHolder(View itemView) {
            super(itemView);
            transactionIdTextView = itemView.findViewById(R.id.text_transaction_id_item);
            transactionTypeTextView = itemView.findViewById(R.id.text_transaction_type_item);
            amountTextView = itemView.findViewById(R.id.text_transaction_amount_item);
            layoutAdminActionWithdraw = itemView.findViewById(R.id.request_admin_action_buttons_layout_item);
            upiIdWithdrawTextView = itemView.findViewById(R.id.text_upi_id_item);
            acceptWithdrawRequest = itemView.findViewById(R.id.accept_request_item);
            declineWithdrawRequest = itemView.findViewById(R.id.decline_request_item);
        }
    }

    private void updateTransactionStatus(String transactionId, boolean isAccepted, TransactionViewHolder holder) {
        holder.acceptWithdrawRequest.setEnabled(false);
        holder.declineWithdrawRequest.setEnabled(false);
        holder.acceptWithdrawRequest.setText("Updating...");
        holder.declineWithdrawRequest.setText("Updating...");

        DocumentReference transactionRef = db.collection("transactions").document(transactionId);

        transactionRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Transaction transaction = documentSnapshot.toObject(Transaction.class);

                    if (transaction != null) {
                        transaction.setPending(false);
                        transaction.setFailed(!isAccepted);

                        transactionRef.set(transaction)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Transaction status updated successfully
                                        updateBalance(transactionId, isAccepted, holder);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Error occurred while updating the transaction status
                                        holder.acceptWithdrawRequest.setEnabled(true);
                                        holder.declineWithdrawRequest.setEnabled(true);
                                        holder.acceptWithdrawRequest.setText("Accept");
                                        holder.declineWithdrawRequest.setText("Decline");
                                    }
                                });
                    }
                }
            }
        });
    }


    private void updateBalance(String transactionId, boolean isAccepted,  TransactionViewHolder holder) {
        DocumentReference transactionRef = db.collection("transactions").document(transactionId);

        transactionRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Transaction transaction = documentSnapshot.toObject(Transaction.class);
                    if (transaction != null) {
                        String userId = transaction.getTransactionUser();
                        int transactionAmount = transaction.getTransactionAmount();

                        DocumentReference walletRef = db.collection("wallets").document(userId);

                        walletRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    double currentBalance = documentSnapshot.getDouble("balance");
                                    if (isAccepted) {
                                        // Update balance for successful withdrawal
                                        double updatedBalance = transaction.isAddMoney()? currentBalance +transactionAmount: currentBalance - transactionAmount;
                                        walletRef.update("balance", updatedBalance)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Balance updated successfully
                                                        holder.acceptWithdrawRequest.setEnabled(true);
                                                        holder.declineWithdrawRequest.setEnabled(true);
                                                        holder.acceptWithdrawRequest.setText("Accept");
                                                        holder.declineWithdrawRequest.setText("Decline");

                                                        holder.layoutAdminActionWithdraw.setVisibility(View.GONE);
                                                        Toast.makeText(holder.transactionIdTextView.getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Error occurred while updating the balance
                                                        // Handle the error, if required
                                                        holder.acceptWithdrawRequest.setEnabled(true);
                                                        holder.declineWithdrawRequest.setEnabled(true);
                                                        holder.acceptWithdrawRequest.setText("Accept");
                                                        holder.declineWithdrawRequest.setText("Decline");
                                                        Toast.makeText(holder.transactionIdTextView.getContext(), "Failed to update wallet", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // Transaction is declined, no need to update the balance
                                        holder.acceptWithdrawRequest.setEnabled(true);
                                        holder.declineWithdrawRequest.setEnabled(true);
                                        holder.acceptWithdrawRequest.setText("Accept");
                                        holder.declineWithdrawRequest.setText("Decline");
                                    }
                                }else{
                                    if (isAccepted) {
                                        // Update balance for successful withdrawal
                                        double updatedBalance = transaction.isAddMoney()? 0.00 +transactionAmount: 0.00 - transactionAmount;
                                        Map<String, Object> walletData = new HashMap<>();
                                        walletData.put("balance", updatedBalance);

                                        walletRef.set(walletData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        holder.acceptWithdrawRequest.setEnabled(true);
                                                        holder.declineWithdrawRequest.setEnabled(true);
                                                        holder.acceptWithdrawRequest.setText("Accept");
                                                        holder.declineWithdrawRequest.setText("Decline");

                                                        holder.layoutAdminActionWithdraw.setVisibility(View.GONE);
                                                        // Wallet document created successfully
                                                        Toast.makeText(holder.transactionIdTextView.getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        holder.acceptWithdrawRequest.setEnabled(true);
                                                        holder.declineWithdrawRequest.setEnabled(true);
                                                        holder.acceptWithdrawRequest.setText("Accept");
                                                        holder.declineWithdrawRequest.setText("Decline");
                                                        // Error creating wallet document
                                                        Toast.makeText(holder.transactionIdTextView.getContext(), "Failed to create wallet", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // Transaction is declined, no need to update the balance
                                        holder.acceptWithdrawRequest.setEnabled(true);
                                        holder.declineWithdrawRequest.setEnabled(true);
                                        holder.acceptWithdrawRequest.setText("Accept");
                                        holder.declineWithdrawRequest.setText("Decline");
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }


}
