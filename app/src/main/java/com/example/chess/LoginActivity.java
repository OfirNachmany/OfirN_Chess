package com.example.chess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private EditText emailField;
    private EditText loginPasswordField;
    private HashMap<String, String> loginInfo;
    private static final String PREF_NAME = AppConstants.PREF_NAME;
    private static final String PREF_KEY_EMAIL = AppConstants.PREF_KEY_EMAIL;
    private static final String PREF_KEY_PASSWORD = AppConstants.PREF_KEY_PASSWORD;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();


        emailField = findViewById(R.id.emailField);
        loginPasswordField = findViewById(R.id.loginPasswordField);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> login());

        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(v -> navigateToSignup());

        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> resetPassword());


        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseApp.initializeApp(this);

        if (currentUser != null) {
            // User is already logged in, navigate to the next screen
            navigateToNextScreen();
        } else {
            String username = getSavedUsername();
            String password = getSavedPassword();
            if (!username.isEmpty() && !password.isEmpty()) {
                // Attempt to log in with the saved credentials
                loginWithCredentials(username, password);
            }
        }
    }
    private void login() {
        String email = emailField.getText().toString().toLowerCase();
        String password = loginPasswordField.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();

                                saveLoginInfo(email, password);

                                navigateToNextScreen();

                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(LoginActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveLoginInfo(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_EMAIL, email);
        editor.putString(PREF_KEY_PASSWORD, password);
        editor.apply();
    }
    private void navigateToSignup() {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }
    private void loginWithCredentials(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Login success, navigate to the next screen
                            navigateToNextScreen();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void navigateToNextScreen() {
        Intent intent = new Intent(getApplicationContext(), SelectTimeDurationActivity.class);
        startActivity(intent);
        finish();
    }
    private String getSavedUsername() {
        return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY_EMAIL, "");
    }
    private String getSavedPassword() {
        return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY_PASSWORD, "");
    }
    private void resetPassword() {
        Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
        startActivity(intent);
    }

}