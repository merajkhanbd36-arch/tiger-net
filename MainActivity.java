package com.tigercall.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private EditText phoneInput, otpInput;
    private Button sendOtpButton, verifyOtpButton, signOutButton;
    private TextView statusText;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        phoneInput = findViewById(R.id.phoneInput);
        otpInput = findViewById(R.id.otpInput);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        signOutButton = findViewById(R.id.signOutButton);
        statusText = findViewById(R.id.statusText);

        sendOtpButton.setOnClickListener(v -> sendOtp());
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
        signOutButton.setOnClickListener(v -> {
            auth.signOut();
            showLogin();
        });

        if (auth.getCurrentUser() != null) {
            showLoggedIn();
        }
    }

    private void sendOtp() {
        String phone = phoneInput.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || !phone.startsWith("+880")) {
            phoneInput.setError("Use Bangladesh format: +8801XXXXXXXXX");
            return;
        }

        statusText.setText("Sending OTP...");
        sendOtpButton.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        sendOtpButton.setEnabled(true);
                        statusText.setText("OTP failed: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Verification failed", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String id,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = id;
                        otpInput.setVisibility(View.VISIBLE);
                        verifyOtpButton.setVisibility(View.VISIBLE);
                        sendOtpButton.setEnabled(true);
                        statusText.setText("OTP sent. Enter the 6-digit code.");
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String code = otpInput.getText().toString().trim();
        if (verificationId == null || code.length() != 6) {
            otpInput.setError("Enter the 6-digit OTP");
            return;
        }
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        String uid = auth.getCurrentUser().getUid();
                        String phone = auth.getCurrentUser().getPhoneNumber();
                        usersRef.child(uid).child("phone").setValue(phone);
                        usersRef.child(uid).child("online").setValue(true);
                        showLoggedIn();
                    } else {
                        statusText.setText("Login failed: " +
                                (task.getException() == null ? "unknown error" :
                                        task.getException().getMessage()));
                    }
                });
    }

    private void showLoggedIn() {
        phoneInput.setVisibility(View.GONE);
        sendOtpButton.setVisibility(View.GONE);
        otpInput.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
        String phone = auth.getCurrentUser() == null ? "" : auth.getCurrentUser().getPhoneNumber();
        statusText.setText("Logged in: " + phone + "\n\nTiger Call backend is connected.\nNext: Audio/Video calling.");
    }

    private void showLogin() {
        phoneInput.setVisibility(View.VISIBLE);
        sendOtpButton.setVisibility(View.VISIBLE);
        otpInput.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.GONE);
        statusText.setText("Enter your Bangladesh phone number.");
    }

    @Override
    protected void onDestroy() {
        if (auth.getCurrentUser() != null) {
            usersRef.child(auth.getCurrentUser().getUid()).child("online").setValue(false);
        }
        super.onDestroy();
    }
}
