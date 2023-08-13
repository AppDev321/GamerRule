package com.gamerrule.android.ui.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Constants;
import com.gamerrule.android.classes.Game;
import com.gamerrule.android.classes.GameAdapter;
import com.gamerrule.android.classes.SlideAdapter;
import com.gamerrule.android.ui.admin.AddNewGameActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

public class HomeActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private WormDotsIndicator dotsIndicator;
    private SlideAdapter slideAdapter;
    private RecyclerView rvGameList;
    private GameAdapter gameAdapter;
    private FirebaseFirestore firestore;
    private TextView walletBalance;
    private ProgressDialog progressDialog;
    private ImageButton logoutButton;

    private Double balance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dot3);
        rvGameList = findViewById(R.id.rv_game_list);
        walletBalance = findViewById(R.id.wallet_balance_home);
        logoutButton = findViewById(R.id.sign_out_home);

        logoutButton.setVisibility(View.GONE);


        firestore = FirebaseFirestore.getInstance();

        // Create and set up the slide adapter
        slideAdapter = new SlideAdapter();
        viewPager.setAdapter(slideAdapter);
        // Attach the dots indicator to the view pager
        dotsIndicator.attachTo(viewPager);

        // Set the layout manager for the RecyclerView as a GridLayoutManager with two columns
        rvGameList.setLayoutManager(new GridLayoutManager(this, 2));

        // Create and set up the adapter for the RecyclerView
        gameAdapter = new GameAdapter();
        rvGameList.setAdapter(gameAdapter);

        // Load the game data from Firestore
        loadGamesFromFirestore();

        // Fetch Wallet balance;
        fetchWalletBalance();
//        walletBalance.setText(new Constants().fetchWalletBalance(HomeActivity.this ) + " ");

        gameAdapter.setOnEditClickListener(new GameAdapter.OnEditClickListener() {
            @Override
            public void onEditClick(Game game) {
                // Handle the edit button click for the specific game
                // You can perform an action or navigate to another screen with the game details
                Intent intent = new Intent(HomeActivity.this, AddNewGameActivity.class);
                intent.putExtra("game", game);
                startActivity(intent);
            }
        });

        gameAdapter.setOnDeleteClickListener(new GameAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(Game game) {
                showDeleteConfirmationDialog(game);
            }
        });


        gameAdapter.setOnItemClickListener(game -> {
            if(game.getEnabled()){
                // Handle item click event
                Intent intent = new Intent(HomeActivity.this, ViewMatchesActivity.class);
                intent.putExtra("selectedGameName", game.getGameName());
                intent.putExtra("selectedGame", game.getDocumentId());
                startActivity(intent);
            }else{
                Toast.makeText(this, game.getGameName() + " is coming Soon.", Toast.LENGTH_SHORT).show();
            }
        });

        walletBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, TransactionsActivity.class));
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationToLogout();
            }
        });
    }

    private void showConfirmationToLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout!!");

        builder.setPositiveButton("Join Now", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
        });

        builder.show();
    }

    private void loadGamesFromFirestore() {
        showLoadingDialog();
        // Query the "games" collection in Firestore
        Query query = firestore.collection("games").orderBy("enabled", Query.Direction.DESCENDING);;

        // Start listening for changes in the query and update the adapter
        query.addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                dismissLoadingDialog();
                gameAdapter.setGames(value.toObjects(Game.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchWalletBalance();
    }

    private double fetchWalletBalance() {
        Log.d("HOME ACTIVITY", "Fetching wallet balance");
        balance = 0.00;


        firestore.collection("wallets").document(FirebaseAuth.getInstance().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        balance = documentSnapshot.getDouble("balance");

                        walletBalance.setText(balance + " ");
                    } else {
                        balance = 0.00;
                        walletBalance.setText(balance + " ");
                    }
                })
                .addOnFailureListener(e -> {
                    walletBalance.setText(0.00 + " ");
                    Log.e("HOME ACTIVITY", "Failed to fetch wallet balance: " + e.getMessage());
                    Toast.makeText(HomeActivity.this, "Failed to fetch wallet balance", Toast.LENGTH_SHORT).show();
                });

        return balance;
    }

    private void showDeleteConfirmationDialog(Game game) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this game?");

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

    private void deleteGame(Game game) {
        showLoadingDialog();
        String gameId = game.getDocumentId();
        DocumentReference gameRef = firestore.collection("games").document(gameId);

        gameRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissLoadingDialog();
                        Toast.makeText(HomeActivity.this, "Game deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissLoadingDialog();
                        Toast.makeText(HomeActivity.this, "Failed to delete game", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}