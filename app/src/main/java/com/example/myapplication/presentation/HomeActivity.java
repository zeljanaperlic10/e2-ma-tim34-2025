package com.example.myapplication.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.presentation.fragments.HomeFragment;
import com.example.myapplication.presentation.fragments.ProfileFragment;
import com.example.myapplication.presentation.fragments.StatsFragment;
import com.example.myapplication.presentation.fragments.StoreFragment;


public class HomeActivity extends AppCompatActivity {


    private BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Učitaj početni fragment (Home)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_store) {
                selectedFragment = new StoreFragment();
            } else if (itemId == R.id.nav_stats) {
                selectedFragment = new StatsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }

            return true;


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

