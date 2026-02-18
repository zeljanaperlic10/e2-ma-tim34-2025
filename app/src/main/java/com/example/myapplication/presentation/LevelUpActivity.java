package com.example.myapplication.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class LevelUpActivity extends AppCompatActivity {

    private TextView tvNewLevel, tvNewTitle, tvNewPp, tvNewRequiredXp, tvBossUnlocked;
    private Button btnFightBoss, btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_up);

        int newLevel = getIntent().getIntExtra("NEW_LEVEL", 1);
        String newTitle = getIntent().getStringExtra("NEW_TITLE");
        int oldPp = getIntent().getIntExtra("OLD_PP", 40);
        int newPp = getIntent().getIntExtra("NEW_PP", 70);
        int newRequiredXp = getIntent().getIntExtra("NEW_REQUIRED_XP", 400);

        tvNewLevel = findViewById(R.id.tvNewLevel);
        tvNewTitle = findViewById(R.id.tvNewTitle);
        tvNewPp = findViewById(R.id.tvNewPp);
        tvNewRequiredXp = findViewById(R.id.tvNewRequiredXp);
        tvBossUnlocked = findViewById(R.id.tvBossUnlocked);
        btnFightBoss = findViewById(R.id.btnFightBoss);
        btnContinue = findViewById(R.id.btnContinue);

        tvNewLevel.setText("Nivo " + newLevel);
        tvNewTitle.setText(newTitle);
        tvNewPp.setText("⚡ PP: " + oldPp + " → " + newPp);
        tvNewRequiredXp.setText("🎯 Sledeći nivo: " + newRequiredXp + " XP");
        tvBossUnlocked.setText("💀 Boss Nivo " + newLevel + " otkljucan!");

        btnFightBoss.setOnClickListener(v -> {
            startActivity(new Intent(this, BossFightActivity.class));
            finish();
        });

        btnContinue.setOnClickListener(v -> finish());
    }
}
