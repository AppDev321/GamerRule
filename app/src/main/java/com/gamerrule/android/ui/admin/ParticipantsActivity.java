package com.gamerrule.android.ui.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Match;
import com.gamerrule.android.classes.Participant;
import com.gamerrule.android.classes.ParticipantAdapter;
import com.gamerrule.android.classes.Transaction;
import com.gamerrule.android.ui.users.TransactionsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ParticipantsActivity extends AppCompatActivity {

    private Match match;
    private RecyclerView recyclerView;
    private ParticipantAdapter participantAdapter;

    private LinearLayout headerLayout;
    private ImageView backButton;
    private TextView titleTextView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        // Retrieve the Match object from the intent extras
        match = (Match) getIntent().getSerializableExtra("match");

        // Set up the header
        headerLayout = findViewById(R.id.headerLayout_participants);
        backButton = findViewById(R.id.iv_back_nav_participants_match);
        titleTextView = findViewById(R.id.match_title_participants);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the activity and go back when the back button is clicked
            }
        });

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.participantsRecyclerView);
        participantAdapter = new ParticipantAdapter(); // Pass the participants list to the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(participantAdapter);

        // Set the title of the match in the header
        titleTextView.setText(match.getImageUrl());

        // Fetch participants
        getParticipantsFromCollection(match.getDocumentId());

        participantAdapter.setButtonClickListener(new ParticipantAdapter.OnParticipantButtonClickListener() {
            @Override
            public void onGiveRewardButtonClick(int position) {
                Participant participant =participantAdapter.getParticipant(position);
                if(participant.isRewarded()){
                    Toast.makeText(ParticipantsActivity.this, "Already Rewarded.", Toast.LENGTH_SHORT).show();
                }else{
                    showRewardDialog(participant);
                }

            }

            @Override
            public void onSeeTransactionsButtonClick(int position) {
                Intent intent = new Intent(ParticipantsActivity.this, TransactionsActivity.class);

                intent.putExtra("uid", participantAdapter.getParticipant(position).getPaticipatedUserId());

                startActivity(intent);
            }
        });
    }

    private void getParticipantsFromCollection(String matchId) {
        CollectionReference participantsCollection = FirebaseFirestore.getInstance().collection("participants");
        Query query = participantsCollection.whereEqualTo("matchId", matchId);
        query.get().addOnSuccessListener(querySnapshot -> {
            List<Participant> participants = new ArrayList<>();
            for (QueryDocumentSnapshot document : querySnapshot) {
                Participant participant = document.toObject(Participant.class);
                participants.add(participant);
            }
            // Call a method to process the retrieved participants data (e.g., update the UI)
            Log.d("PARTICIPANTS LIST", participants.toString());
            participantAdapter.setParticipants(participants);
        }).addOnFailureListener(e -> {
            // Handle failure
        });
    }

    private void showRewardDialog(Participant participant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this
        );
        builder.setTitle("Give Reward");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_give_reward, null);
        builder.setView(dialogView);

        TextView textViewUserName =  dialogView.findViewById(R.id.tv_game_userName_dialog_give_reward);
        EditText editTextKills = dialogView.findViewById(R.id.editTextKills_dialog_give_reward);
        EditText editTextRank = dialogView.findViewById(R.id.editTextRank_dialog_give_reward);
        EditText editTextWinningAmount = dialogView.findViewById(R.id.editTextWinningAmount_dialog_give_reward);

        textViewUserName.setText("Username: " +participant.getGameUsername());
        editTextRank.setText(participant.getRank()+"");
        editTextKills.setText(participant.getKills()+"");
        editTextWinningAmount.setText(participant.getWinningAmount()+"");

        builder.setPositiveButton("Save", (dialog, which) -> {
            String kills = editTextKills.getText().toString().trim();
            String rank = editTextRank.getText().toString().trim();
            String winningAmount = editTextWinningAmount.getText().toString().trim();

            // Perform the necessary actions with the entered data (e.g., save to Firestore)
            saveRewardData(participant, Integer.parseInt(kills), Integer.parseInt(rank), Double.parseDouble(winningAmount), false);
        });

        builder.setNegativeButton("Credit & Save", (dialog, which) -> {
            String kills = editTextKills.getText().toString().trim();
            String rank = editTextRank.getText().toString().trim();
            String winningAmount = editTextWinningAmount.getText().toString().trim();

            // Perform the necessary actions with the entered data (e.g., save to Firestore and credit the winning amount)
            saveRewardData(participant, Integer.parseInt(kills), Integer.parseInt(rank), Double.parseDouble(winningAmount), true);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveRewardData(Participant participant, int kills, int rank, double winningAmount, boolean shouldCredit) {
        // Get the Firestore document reference for the participant
        DocumentReference participantRef = FirebaseFirestore.getInstance()
                .collection("participants")
                .document(participant.getDocumentId());

        // Update the participant's reward data
        participant.setKills(kills);
        participant.setRank(rank);
        participant.setRewarded(shouldCredit);
        participant.setWinningAmount(winningAmount);

        // Show the progress bar
        showProgressDialog();

        // Update the Firestore document with the new reward data
        participantRef.set(participant)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        hideProgressDialog();
                        Toast.makeText(ParticipantsActivity.this, "Reward data saved successfully", Toast.LENGTH_SHORT).show();

                        if (shouldCredit) {
                            // Initiate transaction and credit the winning amount
                            initiateTransaction(participant.getPaticipatedUserId(), winningAmount);
                        }else{
                            // Fetch participants
                            getParticipantsFromCollection(match.getDocumentId());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update the reward data
                        hideProgressDialog();
                        Toast.makeText(ParticipantsActivity.this, "Failed to save reward data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initiateTransaction(String userId, double amount) {
        // Generate transaction ID
        String transactionId = "T" + System.currentTimeMillis();
        // Show the progress bar
        showProgressDialog();
        // Create a new transaction object
        Transaction transaction = new Transaction(transactionId, true, new Date(), (int) amount, userId, false, false, true);


        // Get the Firestore document reference for the transactions collection
        CollectionReference transactionsCollection = FirebaseFirestore.getInstance().collection("transactions");

        // Add the transaction to Firestore
        transactionsCollection.document(transactionId)
                .set(transaction)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Transaction added successfully
                        hideProgressDialog();
                        Toast.makeText(ParticipantsActivity.this, "Transaction initiated successfully", Toast.LENGTH_SHORT).show();

                        // Credit the winning amount to the wallet balance
                        creditWalletBalance(userId, amount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add the transaction
                        hideProgressDialog();
                        Toast.makeText(ParticipantsActivity.this, "Failed to initiate transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void creditWalletBalance(String userId, double amount) {
        // Get the Firestore document reference for the user's wallet
        DocumentReference walletRef = FirebaseFirestore.getInstance()
                .collection("wallets")
                .document(userId);

        showProgressDialog();

        // Update the wallet balance
        walletRef.update("balance", FieldValue.increment(amount))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Wallet balance updated successfully
                        hideProgressDialog();
                        Toast.makeText(ParticipantsActivity.this, "Wallet balance updated", Toast.LENGTH_SHORT).show();
                        getParticipantsFromCollection(match.getDocumentId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update the wallet balance
                        hideProgressDialog();
                        Toast.makeText(ParticipantsActivity.this, "Failed to update wallet balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProgressDialog() {
        // Show the progress bar or any loading indicator
        progressDialog = new ProgressDialog(ParticipantsActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        // Hide the progress bar or loading indicator
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}