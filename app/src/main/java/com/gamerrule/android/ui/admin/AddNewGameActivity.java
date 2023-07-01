package com.gamerrule.android.ui.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Game;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddNewGameActivity extends AppCompatActivity {

    private EditText etGameImageURL;
    private EditText etGameName;
    private EditText etGameDescription;
    private Button addNewGameButton;

    private Boolean isUpdate=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_game);

        etGameImageURL = findViewById(R.id.etGameImageURL);
        etGameName = findViewById(R.id.etGameName);
        etGameDescription = findViewById(R.id.etGameDescription);
        addNewGameButton = findViewById(R.id.addNewGameButton);

        // Check if extra is passed
        if (getIntent().hasExtra("game")) {
            // Get the game object from the extra
            Game game = getIntent().getParcelableExtra("game");
            isUpdate= true;
            // Set initial content with the game object's data
            setInitialContent(game.getImageURL(), game.getGameName(), game.getGameDescription());
        }

        addNewGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                // Get the input values from EditText fields
                String imageURL = etGameImageURL.getText().toString().trim();
                String gameName = etGameName.getText().toString().trim();
                String gameDescription = etGameDescription.getText().toString().trim();

                // Validate the input data
                if (imageURL.isEmpty() || gameName.isEmpty() || gameDescription.isEmpty()) {
                    // Display an error message if any field is empty
                    Toast.makeText(AddNewGameActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                } else {
                    // All input data is valid, proceed to save data to Firestore



                    if(isUpdate){
                        // Create a new game object
                        Game game = new Game(imageURL, gameName, gameDescription);
                        Game gameOld = getIntent().getParcelableExtra("game");
                        game.setDocumentId(gameOld.getDocumentId());
                        updateGameToFirestore(game);
                    }else{
                        saveGameToFirestore(imageURL, gameName, gameDescription);
                    }
                }
            }
        });

    }

    private void saveGameToFirestore(String imageURL, String gameName, String gameDescription) {
        // Show loading state
        setLoading(true);

        // Create a new Game object
        Game game = new Game(imageURL, gameName, gameDescription);

        // Save the game data in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("games")
                .add(game)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Get the generated document ID
                        String documentId = documentReference.getId();

                        // Set the document ID as a field in the Game object
                        game.setDocumentId(documentId);

                        // Update the Firestore document with the document ID field
                        updateGameToFirestore(game);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNewGameActivity.this, "Error adding game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
    }

    private void updateGameToFirestore(Game game){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("games").document(game.getDocumentId())
                .set(game)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNewGameActivity.this, "Game added successfully", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                        if(!isUpdate){
                            setInitialContent("", "", "");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNewGameActivity.this, "Error updating game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
    }

    private void setInitialContent(String imageURL, String gameName, String gameDescription) {
        etGameImageURL.setText(imageURL);
        etGameName.setText(gameName);
        etGameDescription.setText(gameDescription);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            // Disable buttons and EditTexts
            addNewGameButton.setEnabled(false);
            etGameImageURL.setEnabled(false);
            etGameName.setEnabled(false);
            etGameDescription.setEnabled(false);

            // Change button text to "Adding"
            addNewGameButton.setText("Adding");

            // Change button background drawable (replace with your desired drawable)
            addNewGameButton.setBackgroundResource(R.drawable.round_button_disabled);
        } else {
            // Enable buttons and EditTexts
            addNewGameButton.setEnabled(true);
            etGameImageURL.setEnabled(true);
            etGameName.setEnabled(true);
            etGameDescription.setEnabled(true);

            // Change button text back to "Add Game"
            addNewGameButton.setText("Add Game");

            // Change button background drawable back to the original drawable
            addNewGameButton.setBackgroundResource(R.drawable.round_button_primary);
        }
    }

}