package com.gamerrule.android.ui.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.Game;
import com.gamerrule.android.classes.Match;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMatchActivity extends AppCompatActivity {

    private EditText etMatchImageURL, etMap, etMatchType, etPrizePool, etPerKill, etEntryFees, etMaxPlayers, etDescription;
    private Spinner spinnerGameType;
    private Button btnSubmit, btnReset;
    private TextView btnDateTimePicker;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private EditText etRoomId;
    private EditText etRoomPasskey;
    private Spinner spinnerMatchStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_match);

        // Initialize views
        etMatchImageURL = findViewById(R.id.etMatchImageURLAddMatch);
        spinnerGameType = findViewById(R.id.spinnerGameTypeAddMatch);
        btnDateTimePicker = findViewById(R.id.btnDateTimePickerAddMatch);
        etMap = findViewById(R.id.etMapAddMatch);
        etMatchType = findViewById(R.id.etMatchTypeAddMatch);
        etPrizePool = findViewById(R.id.etPrizePoolAddMatch);
        etPerKill = findViewById(R.id.etPerKillAddMatch);
        etEntryFees = findViewById(R.id.etEntryFeesAddMatch);
        etMaxPlayers = findViewById(R.id.etMaxPlayersAddMatch);
        etDescription = findViewById(R.id.etDescriptionAddMatch);
        btnSubmit = findViewById(R.id.btnSubmitAddMatch);
        btnReset = findViewById(R.id.btnResetAddMatch);
        etRoomId = findViewById(R.id.etRoomIdAddMatch);
        etRoomPasskey = findViewById(R.id.etRoomPasskeyAddMatch);
        spinnerMatchStatus = findViewById(R.id.spinnerMatchStatusAddMatch);


        // Set spinner data
        initSpinner();
        // Set spinner options for Match Status
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.match_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMatchStatus.setAdapter(statusAdapter);

        // Set click listener for DateTimePicker
        btnDateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        // Set click listener for Submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values from EditText fields
                String imageUrl = etMatchImageURL.getText().toString().trim();
                String gameType = spinnerGameType.getSelectedItem().toString();
                String matchDateTime = btnDateTimePicker.getText().toString().trim();
                String map = etMap.getText().toString().trim();
                String matchType = etMatchType.getText().toString().trim();
                String prizePool = etPrizePool.getText().toString().trim();
                String perKill = etPerKill.getText().toString().trim();
                String entryFees = etEntryFees.getText().toString().trim();
                String maxPlayers = etMaxPlayers.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String matchStatus = spinnerMatchStatus.getSelectedItem().toString();
                String roomId = etRoomId.getText().toString().trim();
                String roomPasskey = etRoomPasskey.getText().toString().trim();

                // Perform validation checks
                if (TextUtils.isEmpty(imageUrl) || TextUtils.isEmpty(gameType) || TextUtils.isEmpty(matchDateTime)
                        || TextUtils.isEmpty(map) || TextUtils.isEmpty(matchType) || TextUtils.isEmpty(prizePool)
                        || TextUtils.isEmpty(perKill) || TextUtils.isEmpty(entryFees) || TextUtils.isEmpty(maxPlayers)
                        || TextUtils.isEmpty(description)) {
                    // Display an error message for any empty fields
                    Toast.makeText(AddMatchActivity.this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!matchStatus.equals("Upcoming") && (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(roomPasskey))) {
                    // Display an error message if room ID and passkey are empty for non-upcoming matches
                    Toast.makeText(AddMatchActivity.this, "Please enter Room ID and Passkey", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new Match object with the input values
                Match match = new Match();
                match.setImageUrl(imageUrl);
                match.setGameType(gameType);
                try {
                    match.setMatchSchedule(dateFormatter.parse(matchDateTime));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                match.setMap(map);
                match.setMatchType(matchType);
                match.setPrizePool(prizePool);
                match.setPerKill(perKill);
                match.setEntryFees(entryFees);
                match.setMaxPlayers(Integer.parseInt(maxPlayers));
                match.setDescription(description);
                match.setMatchStatus(matchStatus);
                match.setRoomId(roomId);
                match.setRoomPasskey(roomPasskey);

                // Save the Match object to Firestore
                saveMatch(match);
            }
        });

        // Set click listener for Reset button
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetForm();
            }
        });

        // Initialize calendar and date formatter
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

    }

    private void initSpinner() {
        setLoadingState(true);
        // Generate spinner list from Firestore collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference gamesRef = db.collection("games");
        gamesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> gameNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Game game = document.toObject(Game.class);
                        gameNames.add(game.getGameName());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(AddMatchActivity.this,
                            android.R.layout.simple_spinner_item, gameNames);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerGameType.setAdapter(spinnerAdapter);
                    setLoadingState(false);
                } else {
                    setLoadingState(false);
                    // Handle error retrieving data from Firestore
                    Toast.makeText(AddMatchActivity.this, "Errro getting games category", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDateTimePicker() {
        // Get current date and time
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set selected date
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Create time picker dialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(AddMatchActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        // Set selected time
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);

                                        // Format and display selected date and time
                                        String dateTime = dateFormatter.format(calendar.getTime());
                                        btnDateTimePicker.setText(dateTime);
                                    }
                                }, hour, minute, false);

                        // Show time picker dialog
                        timePickerDialog.show();
                    }
                }, year, month, day);

        // Show date picker dialog
        datePickerDialog.show();
    }

    private void saveMatch(Match match) {
        // Disable buttons and input fields during the save process
        setLoadingState(true);

        // Get a reference to the Firestore collection
        CollectionReference matchesCollection = FirebaseFirestore.getInstance().collection("matches");

        // If the match has a document ID, update the existing document
        if (match.getDocumentId() != null) {
            DocumentReference matchDocument = matchesCollection.document(match.getDocumentId());

            // Update the document with the new match data
            matchDocument.set(match)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Match saved successfully
                            setLoadingState(false);
                            Toast.makeText(AddMatchActivity.this, "Match updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Finish the activity after saving
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to save the match
                            setLoadingState(false);
                            Toast.makeText(AddMatchActivity.this, "Failed to update match: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Generate a new document ID for the match
            String documentId = matchesCollection.document().getId();
            match.setDocumentId(documentId);

            // Save the new match document
            matchesCollection.document(documentId)
                    .set(match)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Match saved successfully
                            setLoadingState(false);
                            Toast.makeText(AddMatchActivity.this, "Match added successfully", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to save the match
                            setLoadingState(false);
                            Toast.makeText(AddMatchActivity.this, "Failed to add match: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void resetForm() {
        etMatchImageURL.setText("");
        spinnerGameType.setSelection(0);
        btnDateTimePicker.setText("Match Schedule");
        etMap.setText("");
        etMatchType.setText("");
        etPrizePool.setText("");
        etPerKill.setText("");
        etEntryFees.setText("");
        etMaxPlayers.setText("");
        etDescription.setText("");
        etRoomId.setText("");
        etRoomPasskey.setText("");
        spinnerMatchStatus.setSelection(0);
    }

    private void setLoadingState(boolean isLoading) {
        // Disable/enable buttons and input fields based on isLoading flag
        if (isLoading) {
            // Disable buttons and input fields
            btnSubmit.setEnabled(false);
            btnReset.setEnabled(false);
            etMatchImageURL.setEnabled(false);
            spinnerGameType.setEnabled(false);
            btnDateTimePicker.setEnabled(false);
            etMap.setEnabled(false);
            etMatchType.setEnabled(false);
            etPrizePool.setEnabled(false);
            etPerKill.setEnabled(false);
            etEntryFees.setEnabled(false);
            etMaxPlayers.setEnabled(false);
            etDescription.setEnabled(false);
            etRoomId.setEnabled(false);
            etRoomPasskey.setEnabled(false);
            spinnerMatchStatus.setEnabled(false);

            // Change submit button background drawable
            btnSubmit.setBackgroundResource(R.drawable.round_button_disabled);
            btnSubmit.setText("Processing...");
        } else {
            // Enable buttons and input fields
            btnSubmit.setEnabled(true);
            btnReset.setEnabled(true);
            etMatchImageURL.setEnabled(true);
            spinnerGameType.setEnabled(true);
            btnDateTimePicker.setEnabled(true);
            etMap.setEnabled(true);
            etMatchType.setEnabled(true);
            etPrizePool.setEnabled(true);
            etPerKill.setEnabled(true);
            etEntryFees.setEnabled(true);
            etMaxPlayers.setEnabled(true);
            etDescription.setEnabled(true);
            etRoomId.setEnabled(true);
            etRoomPasskey.setEnabled(true);
            spinnerMatchStatus.setEnabled(true);

            // Change submit button background drawable
            btnSubmit.setBackgroundResource(R.drawable.round_button_primary);
            btnSubmit.setText("Submit");
        }
    }
}