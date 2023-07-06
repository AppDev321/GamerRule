package com.gamerrule.android.ui.users;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Game;
import com.gamerrule.android.classes.GameAdapter;
import com.gamerrule.android.classes.SlideAdapter;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dot3);
        rvGameList = findViewById(R.id.rv_game_list);
        walletBalance = findViewById(R.id.wallet_balance_home);
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

        gameAdapter.setOnItemClickListener(game -> {
            // Handle item click event
            Intent intent = new Intent(HomeActivity.this, ViewMatchesActivity.class);
            intent.putExtra("selectedGame", game.getGameName());
            startActivity(intent);

//            Toast.makeText(this, "Click on " + game.getGameName(), Toast.LENGTH_SHORT).show();
        });

        walletBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, TransactionsActivity.class));
            }
        });
    }

    private void loadGamesFromFirestore() {
        // Query the "games" collection in Firestore
        Query query = firestore.collection("games");

        // Start listening for changes in the query and update the adapter
        query.addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                gameAdapter.setGames(value.toObjects(Game.class));
            }
        });
    }
}