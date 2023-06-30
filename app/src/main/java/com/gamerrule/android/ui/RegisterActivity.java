package com.gamerrule.android.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gamerrule.android.R;
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

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etOtp;
    private TextView tvResendOtp;
    private Button registerButton;

    // variable for FirebaseAuth class
    private FirebaseAuth mAuth;
    // string for storing our verification ID
    private String verificationId;

    private boolean isOtpSent= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI elements
        etName = findViewById(R.id.et_name_input_register);
        etEmail = findViewById(R.id.et_email_input_register);
        etPhone = findViewById(R.id.et_phone_input_register);
        etOtp = findViewById(R.id.et_otp_input_register);
        tvResendOtp = findViewById(R.id.tv_resend_otp_register);
        registerButton = findViewById(R.id.register_button);

        // below line is for getting instance
        // of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            Toast.makeText(this, "Already logged in as " + mAuth.getCurrentUser().getPhoneNumber(), Toast.LENGTH_SHORT).show();
        }

        // Set click listener for the "Resend OTP" TextView
        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logic to resend OTP
                // Replace with your implementation
                Toast.makeText(RegisterActivity.this, "Resending OTP...", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the "Register" Button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform registration logic
                register();
            }
        });
    }

    private void register() {
        setLoading(true);
        // Get user input from EditText fields
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String otp = etOtp.getText().toString().trim();



        // Validate input
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            // Display error message if any of the fields are empty
            Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        // Perform email validation
        if (!isValidEmail(email)) {
            // Display error message if the email is not valid
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        if(!isOtpSent){
            sendVerificationCode(phone);
        }else{
            verifyCode(etOtp.getText().toString());
        }


    }

    private void setLoading(boolean b) {
        if(b){
            etPhone.setEnabled(false);
            etEmail.setEnabled(false);
            etName.setEnabled(false);
            etOtp.setEnabled(false);

            registerButton.setBackgroundResource(R.drawable.round_button_disabled);
            registerButton.setEnabled(false);
        }else{
            etPhone.setEnabled(true);
            etEmail.setEnabled(
                    true
            );
            etName.setEnabled(true);
            etOtp.setEnabled(true);

            registerButton.setBackgroundResource(R.drawable.round_button_primary);
            registerButton.setEnabled(true);
        }
    }

    // Helper method to validate email using regular expression
    private boolean isValidEmail(String email) {
        // Regex pattern for email validation
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        // Compare the entered email with the pattern
        return email.matches(emailPattern);
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
                                String userId = firebaseUser.getUid();

                                // Create a new User object
                                User user = new User(userId, etName.getText().toString(), etEmail.getText().toString(), etPhone.getText().toString());

                                // Save user data in Firestore
                                FirebaseFirestore.getInstance().collection("users").document(userId)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // User data saved successfully
                                                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                // Proceed with further actions or navigate to the next screen
                                                setLoading(false);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Error occurred while saving user data
                                                Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                setLoading(false);
                                            }
                                        });

                            }
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            setLoading(false);
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
            registerButton.setEnabled(true);
            registerButton.setText(getString(R.string.login_send_otp));
            registerButton.setBackgroundResource(R.drawable.round_button_primary);
            Toast.makeText(RegisterActivity.this, "Enter OPT Sent On Phone to verify", Toast.LENGTH_LONG).show();
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
            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

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