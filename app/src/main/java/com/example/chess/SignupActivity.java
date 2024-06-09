package com.example.chess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private EditText newEmailField;
    private EditText newUserPasswordField;
    private EditText newUsernameField;
    private RadioGroup experienceLevelGroup;

    private HashMap<String, String> loginInfo = new HashMap<>();

    private static final String PREF_NAME = AppConstants.PREF_NAME;
    private static final String PREF_KEY_EMAIL = AppConstants.PREF_KEY_EMAIL;
    private static final String PREF_KEY_PASSWORD = AppConstants.PREF_KEY_PASSWORD;
    private static final String PREF_KEY_UID = AppConstants.PREF_KEY_UID;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();

        newEmailField = findViewById(R.id.newEmailField);
        newUsernameField = findViewById(R.id.newUsernameField);
        newUserPasswordField = findViewById(R.id.newUserPasswordField);
        experienceLevelGroup = findViewById(R.id.experienceLevelGroup);
        Button createAccountButton = findViewById(R.id.createAccountButton);
        Button loginButton = findViewById(R.id.loginButton);

        createAccountButton.setOnClickListener(v -> createAccount());

        loginButton.setOnClickListener(v -> navigateToLogin());
        // Retrieve existing user data
        loginInfo = getExistingUserData();
    }

    private HashMap<String, String> getExistingUserData() {
        HashMap<String, String> existingData = new HashMap<>();

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            // Assume that user data keys are stored as usernames
            // and values are passwords (you might want to add more checks in a real app)
            existingData.put(entry.getKey(), entry.getValue().toString());
        }

        return existingData;
    }


    private void createAccount() {
        String email = String.valueOf(newEmailField.getText().toString());
        String password = String.valueOf(newUserPasswordField.getText().toString());
        String username = newUsernameField.getText().toString();


        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedExperienceLevelId = experienceLevelGroup.getCheckedRadioButtonId();

        if (selectedExperienceLevelId == -1) {
            // No experience level selected
            Toast.makeText(this, "Select experience level", Toast.LENGTH_SHORT).show();
            return;
        }

        int rating;
        if (selectedExperienceLevelId == R.id.newToChessRadioButton) {
            rating = 400;
        } else if (selectedExperienceLevelId == R.id.BeginnerRadioButton) {
            rating = 800;
        } else if (selectedExperienceLevelId == R.id.IntermediateRadioButton) {
            rating = 1200;
        } else if (selectedExperienceLevelId == R.id.AdvancedRadioButton) {
            rating = 1600;
        } else {
            rating = 800;
        }

        // Check if the email is already registered
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                                // Email already registered
                                Toast.makeText(SignupActivity.this, "Email already registered.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                // Email not registered, proceed with account creation
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Retrieve the newly created user
                                                    FirebaseUser user = mAuth.getCurrentUser();

                                                    if (user != null) {
                                                        // Save user data to SharedPreferences
                                                        saveUserDataToFile(user.getUid(), email, password);

                                                        saveAdditionalData(user.getUid(), username, rating, rating, rating);

                                                        // Navigate to the LoginActivity
                                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Error occurred during email check
                            Toast.makeText(SignupActivity.this, "Error checking email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    // Save user data with UID
    private void saveUserDataToFile(String uid, String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_EMAIL, email);
        editor.putString(PREF_KEY_PASSWORD, password);
        editor.putString(PREF_KEY_UID, uid);
        editor.apply();
    }

    private void saveAdditionalData(String uid, String username, int bulletRating, int blitzRating, int rapidRating) {
        // Create a new document with a generated ID
        DocumentReference userRef = db.collection("users").document(uid);

        // Set the username field in the document
        userRef.set(new HashMap<String, Object>() {
                    {
                        put("username", username);
                        put("bulletRating", bulletRating);
                        put("blitzRating", blitzRating);
                        put("rapidRating", rapidRating);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Username saved successfully to Fire store.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "Error saving username to Fire store: ", e);
                    }
                });
    }


    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}