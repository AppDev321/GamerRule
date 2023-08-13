package com.gamerrule.android.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.MainActivity;
import com.gamerrule.android.R;
import com.gamerrule.android.classes.Constants;
import com.gamerrule.android.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private TextView tvRegister;
    private Button loginButton;
    private EditText etPhone, etOtp;

    private boolean isOtpSent= false;

    // variable for FirebaseAuth class
    private FirebaseAuth mAuth;
    // string for storing our verification ID
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvRegister= findViewById(R.id.tv_register);
        loginButton = findViewById(R.id.submit_button_login);
        etPhone = findViewById(R.id.et_phone_input);
        etOtp = findViewById(R.id.et_login_otp);

        // below line is for getting instance
        // of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
//            Toast.makeText(this, "Already logged in as " + mAuth.getCurrentUser().getPhoneNumber(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

    }

    private void login() {
        setLoading(true);


        // Verify if the phone number is valid
        if (!isValidPhoneNumber(etPhone.getText().toString())) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        etPhone.setText(new Constants().makeValidPhoneNumber(etPhone.getText().toString()));


        String phoneNumber = etPhone.getText().toString();

        // Check if a user with the given phone number exists in Firestore
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("phone", phoneNumber)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (querySnapshot.isEmpty()) {
                            // No user found with the given phone number
                            Toast.makeText(LoginActivity.this, "No user exists with this phone number", Toast.LENGTH_SHORT).show();
                        } else {
                            // User found, proceed with login logic
                            // ...
                            if(isOtpSent){
                                verifyCode(etOtp.getText().toString().trim());
                            }else{
                                sendVerificationCode(new Constants().makeValidPhoneNumber(phoneNumber));
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred while fetching user data
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Add your phone number validation logic here
        // You can use regular expressions or any other validation method to check if the phone number is valid
        // For simplicity, let's assume a valid phone number is 10 digits long
        return phoneNumber.length() >= 10;
    }

    private void setLoading(boolean b) {
        if(b){
            etPhone.setEnabled(false);
            etOtp.setEnabled(false);

            loginButton.setBackgroundResource(R.drawable.round_button_disabled);
            loginButton.setEnabled(false);
        }else{
            etPhone.setEnabled(true);
            etOtp.setEnabled(true);

            loginButton.setBackgroundResource(R.drawable.round_button_primary);
            loginButton.setEnabled(true);
        }
    }





    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // User registration successful
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                setLoading(false);
                                Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_LONG).show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 1000);

                            }
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }


    private void sendVerificationCode(String number) {
        // this method is used for getting
        // OTP on user phone number.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // callback method is called on Phone auth provider.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;

            isOtpSent = true;
            etOtp.setEnabled(true);
            etOtp.setVisibility(View.VISIBLE);
            loginButton.setEnabled(true);
            loginButton.setText(getString(R.string.login_send_otp));
            loginButton.setBackgroundResource(R.drawable.round_button_primary);
            Toast.makeText(LoginActivity.this, "Enter OPT Sent On Phone to verify", Toast.LENGTH_LONG).show();
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
                etOtp.setText(code);

                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

            setLoading(false);
        }
    };

    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting
        // credentials from our verification id and code.
        setLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }
}