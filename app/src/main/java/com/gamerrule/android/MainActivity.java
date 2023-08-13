package com.gamerrule.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.gamerrule.android.classes.Constants;
import com.gamerrule.android.ui.AdminActivity;
import com.gamerrule.android.ui.LoginActivity;
import com.gamerrule.android.ui.users.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private boolean isAdmin;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            if(new Constants().isUserAdmin()){
                intent = new Intent(MainActivity.this, AdminActivity.class);
            }else{
                intent = new Intent(MainActivity.this, HomeActivity.class);
            }
        }else
            intent = new Intent(MainActivity.this, LoginActivity.class);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 3000);
    }
}