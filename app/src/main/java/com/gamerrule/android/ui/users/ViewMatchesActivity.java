package com.gamerrule.android.ui.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Game;
import com.gamerrule.android.classes.GameId;
import com.gamerrule.android.classes.Match;
import com.gamerrule.android.classes.MatchesAdapter;
import com.gamerrule.android.classes.Participant;
import com.gamerrule.android.classes.Transaction;
import com.gamerrule.android.ui.admin.AddMatchActivity;
import com.gamerrule.android.ui.admin.ParticipantsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewMatchesActivity extends AppCompatActivity {

    private static final String TAG = "ViewMatchesActivity";

    private RecyclerView recyclerViewMatches;
    private MatchesAdapter matchesAdapter;
    private List<Match> matchesList;
    private TextView gameName;
    private Match selectedMatch;
    private String extraValue;
    private boolean isAdmin = false;

    private ProgressDialog progressDialog;

    private double balance = 0.00;
    private String transactionId;
    private String userIdGame;

    private CollectionReference matchesCollectionRef;
    private CollectionReference gameIdsRef;
    private CollectionReference participantsRef;
    private DocumentReference walletRef;
    private double walletBalanceUser = 0.0;
    private boolean walletLoaded= false;
    private ImageView ivBackButton, ivEditGameIdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_matches);

        gameName = findViewById(R.id.game_title_match);
        recyclerViewMatches = findViewById(R.id.recyclerViewMatches);
        ivBackButton = findViewById(R.id.iv_back_nav_view_match);
        ivEditGameIdButton = findViewById(R.id.iv_edit_gameId_view_match);



        recyclerViewMatches.setLayoutManager(new LinearLayoutManager(this));

        transactionId = "T" + System.currentTimeMillis();

        Intent intent = getIntent();
        if (intent.hasExtra("selectedGame")) {
            extraValue = intent.getStringExtra("selectedGame");
            gameName.setText(intent.getStringExtra("selectedGameName"));
        }
        ivEditGameIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGameIdDocument(FirebaseAuth.getInstance().getUid(), extraValue, true);
                selectedMatch = null;
            }
        });



        ivBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Initialize Firestore references
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        matchesCollectionRef = db.collection("matches");
        gameIdsRef = db.collection("gameids");
        participantsRef = db.collection("participants");
        walletRef = db.collection("wallets").document(FirebaseAuth.getInstance().getCurrentUser().getUid());


        // Initialize matches list and adapter
        matchesList = new ArrayList<>();
        matchesAdapter = new MatchesAdapter(matchesList, isAdmin,
                FirebaseAuth.getInstance().getUid(), ViewMatchesActivity.this);
        recyclerViewMatches.setAdapter(matchesAdapter);

        // Set join button click listener
        matchesAdapter.setJoinButtonClickListener(new MatchesAdapter.OnJoinButtonClickListener() {
            @Override
            public void onJoinButtonClick(int position) {
                Match match = matchesAdapter.getMatch(position);
                selectedMatch = match;
                String userId = FirebaseAuth.getInstance().getUid();

                // Check if the user has the "userid" collection in Firestore
                checkGameIdDocument(userId, match.getGameType(), false);
            }
        });

        matchesAdapter.setButtonClickListener(new MatchesAdapter.OnButtonClickListener() {
            @Override
            public void onEditButtonClick(int position) {
                Intent intent1 = new Intent(ViewMatchesActivity.this, AddMatchActivity.class);
                intent1.putExtra("selectedMatch", matchesAdapter.getMatch(position));
                startActivity(intent1);
            }

            @Override
            public void onDeleteButtonClick(int position) {
                showDeleteConfirmationDialog(matchesAdapter.getMatch(position));
            }

            @Override
            public void onRewardsButtonClick(int position) {
                Intent intent1 = new Intent(ViewMatchesActivity.this, ParticipantsActivity.class);
                intent1.putExtra("match", matchesAdapter.getMatch(position));
                startActivity(intent1);
            }

            @Override
            public void onViewParticipantsButtonClick(int position) {
                Intent intent1 = new Intent(ViewMatchesActivity.this, ParticipantsActivity.class);
                intent1.putExtra("match", matchesAdapter.getMatch(position));
                startActivity(intent1);
            }

            @Override
            public void onViewRoomIdButtonClick(int position) {
                showRoomIdToJoin(matchesAdapter.getMatch(position));
            }
        });

        // Retrieve matches from Firestore
        retrieveMatchesFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isGameIdDocument(FirebaseAuth.getInstance().getUid(), extraValue);
        fetchWalletBalance(FirebaseAuth.getInstance().getUid());
    }

    private void retrieveMatchesFromFirestore() {
        Log.d(TAG, "Retrieving matches from Firestore");

        showProgressDialog();

        Query query;
        if (!extraValue.isEmpty()) {
            query = matchesCollectionRef.whereEqualTo("gameType", extraValue).orderBy("matchSchedule");
//            .orderBy("matchStatus").orderBy("matchSchedule", Query.Direction.ASCENDING)
        } else {
            query = matchesCollectionRef.orderBy("matchSchedule", Query.Direction.DESCENDING);
        }

        query.get().addOnSuccessListener(querySnapshot -> {
            List<Match> matches = querySnapshot.toObjects(Match.class);
            matchesAdapter.setMatches(matches);

            Log.d(TAG, "Matches retrieved successfully");
            hideProgressDialog();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error retrieving matches: " + e.getMessage());
            hideProgressDialog();
        });
    }

    private void isGameIdDocument(String userId, String currentGameName) {
        Log.d(TAG, "Checking if game ID document exists");

        Query query = gameIdsRef.whereEqualTo("userId", userId)
                .whereEqualTo("gameName", currentGameName)
                .limit(1);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {

                    Log.d(TAG, "Game ID document exists "+ querySnapshot.getDocuments().get(0).toObject(GameId.class).getGameId());
                    ivEditGameIdButton.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "Game ID document does not exist");
                    ivEditGameIdButton.setVisibility(View.GONE);
                }
            } else {
                Log.e(TAG, "Failed to fetch game ID: " + task.getException().getMessage());
                ivEditGameIdButton.setVisibility(View.GONE);
                Toast.makeText(ViewMatchesActivity.this, "Failed to fetch game ID", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkGameIdDocument(String userId, String currentGameName, boolean isEdit) {
        Log.d(TAG, "Checking if game ID document exists");

        showProgressDialog();

        Query query = gameIdsRef.whereEqualTo("userId", userId)
                .whereEqualTo("gameName", currentGameName)
                .limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    GameId gameId = document.toObject(GameId.class);
                    userIdGame = gameId.getGameId();
                    Log.d(TAG, "Game ID document exists");
                    hideProgressDialog();

                    if(isEdit){
                        showGameIdInputDialog(userId, currentGameName, gameId, isEdit);
                    }else
                        showConfirmationToJoin(gameId.getGameId());
                } else {
                    Log.d(TAG, "Game ID document does not exist");
                    hideProgressDialog();

                    showGameIdInputDialog(userId, currentGameName, null, false);
                }
            } else {
                Log.e(TAG, "Failed to fetch game ID: " + task.getException().getMessage());
                hideProgressDialog();
                Toast.makeText(ViewMatchesActivity.this, "Failed to fetch game ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGameIdInputDialog(String userId, String currentGameName, GameId userIdGameNew, boolean isEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Game Username");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // Set padding in dp to the LinearLayout
        int paddingInDp = 16;
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPx = (int) (paddingInDp * scale + 0.5f); // Convert dp to pixels
        linearLayout.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

        // Create the EditText
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Add the EditText to the LinearLayout
        linearLayout.addView(input);

        if(userIdGameNew != null){
            builder.setTitle("Edit Game Username");
            input.setText(userIdGameNew.getGameId());
        }else{
            builder.setTitle("Enter Game Username");
        }

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.setMargins(16,16,16,16);
        input.setLayoutParams(buttonLayoutParams);
        input.setHint("Your Game Username");
        builder.setView(linearLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String gameId = input.getText().toString().trim();
            if (!TextUtils.isEmpty(gameId)) {
                if(isEdit){
                    userIdGameNew.setGameId(gameId);
                    updateGameIdDocument(userIdGameNew, isEdit);
                }else
                    createGameIdDocument(userId, currentGameName, gameId);
            } else {
                Toast.makeText(ViewMatchesActivity.this, "Please enter a valid Game ID", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createGameIdDocument(String userId, String currentGameName, String gameId) {
        Log.d(TAG, "Creating game ID document");

        showProgressDialog();

        GameId gameIdObj = new GameId(null, gameId, userId, currentGameName);

        gameIdsRef.add(gameIdObj)
                .addOnSuccessListener(documentReference -> {
                    String newGameId = documentReference.getId();
                    gameIdObj.setDocumentId(newGameId);

                    updateGameIdDocument(gameIdObj, false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create game ID document: " + e.getMessage());
                    hideProgressDialog();
                    Toast.makeText(ViewMatchesActivity.this, "Failed to create game ID document", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGameIdDocument(GameId gameIdObj, boolean isEdit) {
        String documentId = gameIdObj.getDocumentId();
        showProgressDialog();
        if (documentId != null) {
            Log.d(TAG, "Updating game ID document");

            gameIdsRef.document(documentId).set(gameIdObj)
                    .addOnSuccessListener(aVoid -> {
                        hideProgressDialog();
                        userIdGame = gameIdObj.getGameId();
                        if(!isEdit && selectedMatch != null)
                            showConfirmationToJoin(gameIdObj.getGameId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update game ID document: " + e.getMessage());
                        hideProgressDialog();
                        Toast.makeText(ViewMatchesActivity.this, "Failed to update game ID document", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "Failed to obtain game ID document ID");
            hideProgressDialog();
            Toast.makeText(ViewMatchesActivity.this, "Failed to obtain game ID document ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationToJoin(String gameId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Game");
        builder.setMessage("Are you sure you want to join this game with username: " +
                gameId + "?" + "\n\nEntry fee will be debited from the wallet!!\n\n"+"To edit user name click on the edit button on header.");

        builder.setPositiveButton("Join Now", (dialog, which) -> {
            generateTransaction();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            Toast.makeText(ViewMatchesActivity.this, "Cancelled joining the game", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void generateTransaction() {
        Log.d(TAG, "Generating transaction");

        showProgressDialog();

        boolean addMoney = false;
        Date transactionTime = new Date();
        int transactionAmount = Integer.parseInt(selectedMatch.getEntryFees());
        String transactionUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Transaction transaction = new Transaction(transactionId, addMoney, transactionTime, transactionAmount, transactionUser,
                false, false, true);

        if(!walletLoaded){
            Toast.makeText(this, "Your wallet balance has not loaded. Please try again.", Toast.LENGTH_LONG).show();
            hideProgressDialog();
            return;
        }

        if (walletBalanceUser >= transactionAmount) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("transactions").document(transactionId).set(transaction)
                    .addOnSuccessListener(aVoid -> updateWalletBalance(transactionUser, (-1 * transactionAmount)))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save transaction: " + e.getMessage());
                        hideProgressDialog();
                        Toast.makeText(ViewMatchesActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(ViewMatchesActivity.this, "Insufficient balance "+ walletBalanceUser +" try again.", Toast.LENGTH_SHORT).show();
            hideProgressDialog();
        }
    }

    private void updateWalletBalance(String userId, double transactionAmount) {
        Log.d(TAG, "Updating wallet balance");

        walletRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    walletRef.update("balance", FieldValue.increment(transactionAmount))
                            .addOnSuccessListener(aVoid -> addParticipantToCollection(userIdGame))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update balance: " + e.getMessage());
                                hideProgressDialog();
                                Toast.makeText(ViewMatchesActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Map<String, Object> walletData = new HashMap<>();
                    walletData.put("balance", transactionAmount);

                    walletRef.set(walletData)
                            .addOnSuccessListener(aVoid -> addParticipantToCollection(userIdGame))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to create wallet: " + e.getMessage());
                                hideProgressDialog();
                                Toast.makeText(ViewMatchesActivity.this, "Failed to create wallet", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Log.e(TAG, "Failed to retrieve wallet data: " + task.getException().getMessage());
                hideProgressDialog();
                Toast.makeText(ViewMatchesActivity.this, "Failed to retrieve wallet data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double fetchWalletBalance(String userId) {
        Log.d(TAG, "Fetching wallet balance");

        walletRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        balance = documentSnapshot.getDouble("balance");
                        walletBalanceUser = balance;
                    } else {
                        balance = 0.00;
                        walletBalanceUser = balance;
                    }
                    walletLoaded = true;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch wallet balance: " + e.getMessage());
                    Toast.makeText(ViewMatchesActivity.this, "Failed to fetch wallet balance", Toast.LENGTH_SHORT).show();
                });

        return balance;
    }

    private void addParticipantToCollection(String gameUsername) {
        Log.d(TAG, "Adding participant to collection");

        Participant participant = new Participant(selectedMatch.getDocumentId(),
                new Date(), FirebaseAuth.getInstance().getUid(), Double.parseDouble(selectedMatch.getEntryFees()),
                gameUsername, 0, 0, 0.00);

        participantsRef.add(participant)
                .addOnSuccessListener(documentReference -> {
                    String participantId = documentReference.getId();
                    participant.setDocumentId(participantId);

                    updateParticipantDocument(participant);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add participant: " + e.getMessage());
                    hideProgressDialog();
                    Toast.makeText(ViewMatchesActivity.this, "Failed to add participant", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateParticipantDocument(Participant participant) {
        String documentId = participant.getDocumentId();

        if (documentId != null) {
            Log.d(TAG, "Updating participant document");

            participantsRef.document(documentId).set(participant)
                    .addOnSuccessListener(aVoid -> {
                        hideProgressDialog();
                        Toast.makeText(ViewMatchesActivity.this, "Joined Game successfully", Toast.LENGTH_SHORT).show();
                        retrieveMatchesFromFirestore();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update participant document: " + e.getMessage());
                        hideProgressDialog();
                        Toast.makeText(ViewMatchesActivity.this, "Failed to Join", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "Failed to obtain participant ID");
            hideProgressDialog();
            Toast.makeText(ViewMatchesActivity.this, "Failed to obtain participant ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(Match game) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this match?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteGame(game);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing or perform any desired action on cancel
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteGame(Match game) {
        showProgressDialog();
        String gameId = game.getDocumentId();
        DocumentReference gameRef = FirebaseFirestore.getInstance().collection("matches").document(gameId);

        gameRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressDialog();
                        Toast.makeText(ViewMatchesActivity.this, "Game deleted successfully", Toast.LENGTH_SHORT).show();
                        retrieveMatchesFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressDialog();
                        Toast.makeText(ViewMatchesActivity.this, "Failed to delete game", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRoomIdToJoin(Match match) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Room Details");
        builder.setMessage("Use these details to join this game: " +
                "\n\nRoom ID - "+ match.getRoomId()+"\nPassword - " + match.getRoomPasskey());

        builder.setPositiveButton("Join Now", (dialog, which) -> {
//            generateTransaction();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
//            Toast.makeText(ViewMatchesActivity.this, "Cancelled joining the game", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null ) {
            progressDialog.dismiss();
        }else {
            Log.d(TAG, "Failed to close progressDialog" + progressDialog + progressDialog.isShowing());
        }
    }

}
