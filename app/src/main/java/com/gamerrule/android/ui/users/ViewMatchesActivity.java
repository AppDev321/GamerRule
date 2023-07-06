package com.gamerrule.android.ui.users;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Match;
import com.gamerrule.android.classes.MatchesAdapter;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewMatchesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMatches;
    private MatchesAdapter matchesAdapter;
    private List<Match> matchesList;
    private TextView gameName;
    String extraValue;
    boolean isAdmin= false;
    // Firestore collection reference
    private CollectionReference matchesCollectionRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_matches);

        gameName= findViewById(R.id.game_title_match);
        // Initialize RecyclerView
        recyclerViewMatches = findViewById(R.id.recyclerViewMatches);
        recyclerViewMatches.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        if(intent.hasExtra("selectedGame")){
            extraValue = intent.getStringExtra("selectedGame");
            gameName.setText(extraValue);
        }

        // Initialize matches list and adapter
        matchesList = new ArrayList<>();
        matchesAdapter = new MatchesAdapter(matchesList, isAdmin);

        // Set adapter on RecyclerView
        recyclerViewMatches.setAdapter(matchesAdapter);

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        matchesCollectionRef = db.collection("matches");

        // Retrieve matches from Firestore and update the matches list
        retrieveMatchesFromFirestore();
    }

    private void retrieveMatchesFromFirestore() {
        // Query matches collection ordered by schedule date

        Query query;
        if(!extraValue.isEmpty())
        {
            query = matchesCollectionRef.whereEqualTo("gameType", extraValue).orderBy("matchSchedule");

        }else{
            query = matchesCollectionRef.orderBy("matchSchedule");

        }

        // Execute the query
        Task<QuerySnapshot> task = query.get();
        task.addOnSuccessListener(querySnapshot -> {
            // Process the query results
            List<Match> matches = querySnapshot.toObjects(Match.class);
            // Update the adapter with the retrieved matches
            Log.d("TAG", matches.toString());
            matchesAdapter.setMatches(matches);
        }).addOnFailureListener(e -> {
            // Handle any errors
            Log.e("FETCH MATCH ", "Error retrieving matches: " + e.getMessage());
        });
    }
}