package com.example.myapplication.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.util.SharedPrefsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.onesignal.OneSignal;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginBtn, goToRegisterBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefsManager = new SharedPrefsManager(this);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        goToRegisterBtn = findViewById(R.id.goToRegisterBtn);

        // Ako je korisnik već prijavljen i aktiviran, preskoči login
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            openMainScreen();
        }

        loginBtn.setOnClickListener(v -> loginUser());
        goToRegisterBtn.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));



    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Unesi email i lozinku!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            String userId = user.getUid();
                            // Poveži OneSignal sa Firebase User ID
                            OneSignal.login(userId);

                            // Čuvaj OneSignal Player ID u Firestore (opciono ali korisno)
                            String oneSignalPlayerId = OneSignal.getUser().getOnesignalId();
                            if (oneSignalPlayerId != null) {
                                db.collection("users").document(userId)
                                        .update("oneSignalId", oneSignalPlayerId)
                                        .addOnFailureListener(e -> {
                                            // Ako field ne postoji, kreiraj ga
                                            db.collection("users").document(userId)
                                                    .update("oneSignalId", oneSignalPlayerId);
                                        });
                            }
                            // čuvamo status u SharedPreferences
                            prefsManager.saveUserSession(user.getUid(), email);
                            Toast.makeText(this, "Uspešno prijavljivanje!", Toast.LENGTH_SHORT).show();
                            openMainScreen();
                        } else {
                            Toast.makeText(this, "Nalog nije aktiviran! Proveri email.", Toast.LENGTH_LONG).show();
                            auth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "Pogrešan email ili lozinka!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMainScreen() {
        startActivity(new Intent(this, HomeActivity.class)); // promenjeno sa MainActivity
        finish();
    }


}
