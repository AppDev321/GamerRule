package com.gamerrule.android.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gamerrule.android.R;
import com.gamerrule.android.ui.admin.AddMatchActivity;
import com.gamerrule.android.ui.admin.AddNewGameActivity;
import com.gamerrule.android.ui.users.HomeActivity;

public class AdminActivity extends AppCompatActivity {

    // Declare global variables
    private ImageView ivHomeNavAdminPanel;
    private TextView tvAdminPanel;
    private LinearLayout addGameLayout;
    private LinearLayout addMatchLayout;
    private LinearLayout viewGameLayout;
    private LinearLayout viewTransactionsLayout;
    private LinearLayout withdrawRequestsLayout;
    private LinearLayout manageUsersLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize the global variables
        ivHomeNavAdminPanel = findViewById(R.id.iv_home_nav_admin_panel);
        addGameLayout = findViewById(R.id.add_game_ap);
        addMatchLayout = findViewById(R.id.add_match_ap);
        viewGameLayout = findViewById(R.id.view_game_ap);
        viewTransactionsLayout = findViewById(R.id.view_transition);
        withdrawRequestsLayout = findViewById(R.id.withdraw_request_ap);
        manageUsersLayout = findViewById(R.id.manage_users_ap);

        // Set click listeners for the options if needed
        addGameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event for Add Game option
                startActivity(new Intent(AdminActivity.this, AddNewGameActivity.class));
            }
        });

        addMatchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event for Add Match option
                startActivity(new Intent(AdminActivity.this, AddMatchActivity.class));
            }
        });

        viewGameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event for View Games option
                startActivity(new Intent(AdminActivity.this, HomeActivity.class));
            }
        });

        viewTransactionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event for View Transactions option
            }
        });

        withdrawRequestsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event for Withdraw Requests option
            }
        });

        manageUsersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event for Manage Users option
            }
        });
    }
}