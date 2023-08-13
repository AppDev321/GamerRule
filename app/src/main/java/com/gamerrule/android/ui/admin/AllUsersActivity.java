package com.gamerrule.android.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.gamerrule.android.R;
import com.gamerrule.android.classes.User;
import com.gamerrule.android.classes.UsersAdapter;
import com.gamerrule.android.ui.users.TransactionsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private ImageButton backButton;
    private UsersAdapter usersAdapter;
    private ProgressDialog progressDialog;

    List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        // Initialize views
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        backButton = findViewById(R.id.ib_back_view_users);

        userList = new ArrayList<>();
        // Set up RecyclerView and Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        usersAdapter = new UsersAdapter(userList);
        recyclerViewUsers.setLayoutManager(layoutManager);
        recyclerViewUsers.setAdapter(usersAdapter);

        // Set click listener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Fetch user data from Firestore
        fetchUsers();

        usersAdapter.setTransactionButtonClickListener(new UsersAdapter.OnTransactionButtonClickListener() {
            @Override
            public void onTransactionButtonClick(int position) {
                Intent intent = new Intent(AllUsersActivity.this, TransactionsActivity.class);

                intent.putExtra("uid", userList.get(position).getUid());

                startActivity(intent);
            }
        });
    }

    private void fetchUsers() {
        // Show loading dialog
        showProgressDialog();

        // Get reference to "users" collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        // Query users collection
        Query query = usersRef.orderBy("name");

        // Execute query
        query.get().addOnSuccessListener(querySnapshot -> {
           userList = new ArrayList<>();

            // Iterate through the query snapshot and create User objects
            for (User user : querySnapshot.toObjects(User.class)) {
                userList.add(user);
            }

            // Update the adapter with the user list
            usersAdapter.setUserList(userList);

            // Hide loading dialog
            hideProgressDialog();
        }).addOnFailureListener(e -> {
            // Handle failure
            // Hide loading dialog
            hideProgressDialog();
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}