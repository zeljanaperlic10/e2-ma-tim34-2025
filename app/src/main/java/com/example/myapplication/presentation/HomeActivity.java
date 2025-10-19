package com.example.myapplication.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private Button logoutBtn, addTaskBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logoutBtn = findViewById(R.id.logoutBtn);
        addTaskBtn = findViewById(R.id.addTaskBtn);

        // Logout dugme
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        // Dodaj zadatak dugme
        addTaskBtn.setOnClickListener(v -> {
            // Preuzmi UID trenutnog korisnika
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Otvori CreateTaskActivity i prosledi userId
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }
}

