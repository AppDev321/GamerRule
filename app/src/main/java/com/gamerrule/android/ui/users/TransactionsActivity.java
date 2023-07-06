package com.gamerrule.android.ui.users;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gamerrule.android.R;

public class TransactionsActivity extends AppCompatActivity {

    private Button addBalnce;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        addBalnce= findViewById(R.id.add_balance_transaction);

        addBalnce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TransactionsActivity.this, AddBalanceActivity.class));
            }
        });
    }
}