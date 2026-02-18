package com.example.myapplication.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Boss;
import com.example.myapplication.data.model.Equipment;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.repository.BossRepository;
import com.example.myapplication.data.repository.EquipmentRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.presentation.adapters.StatsEquipmentAdapter;
import com.example.myapplication.presentation.adapters.StatsBossAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private TextView tvStatsLevel, tvStatsTitle, tvStatsXp, tvStatsPp, tvStatsCoins;
    private TextView tvNoEquipmentStats, tvNoBossesStats;
    private ProgressBar progressXp;
    private RecyclerView recyclerStatsEquipment, recyclerStatsBosses;

    private UserRepository userRepository = new UserRepository();
    private EquipmentRepository equipmentRepository = new EquipmentRepository();
    private BossRepository bossRepository = new BossRepository();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvStatsLevel = findViewById(R.id.tvStatsLevel);
        tvStatsTitle = findViewById(R.id.tvStatsTitle);
        tvStatsXp = findViewById(R.id.tvStatsXp);
        tvStatsPp = findViewById(R.id.tvStatsPp);
        tvStatsCoins = findViewById(R.id.tvStatsCoins);
        tvNoEquipmentStats = findViewById(R.id.tvNoEquipmentStats);
        tvNoBossesStats = findViewById(R.id.tvNoBossesStats);
        progressXp = findViewById(R.id.progressXp);
        recyclerStatsEquipment = findViewById(R.id.recyclerStatsEquipment);
        recyclerStatsBosses = findViewById(R.id.recyclerStatsBosses);

        recyclerStatsEquipment.setLayoutManager(new LinearLayoutManager(this));
        recyclerStatsBosses.setLayoutManager(new LinearLayoutManager(this));

        loadUserStats();
        loadEquipment();
        loadBosses();
    }

    private void loadUserStats() {
        userRepository.getUser(userId, new UserRepository.OnUserLoaded() {
            @Override
            public void onSuccess(User user) {
                tvStatsLevel.setText("Nivo " + user.getLevel());
                tvStatsTitle.setText(user.getTitle());
                tvStatsXp.setText(user.getXp() + " / " + user.getRequiredXp() + " XP");
                tvStatsPp.setText(user.getPp() + " PP");
                tvStatsCoins.setText(user.getCoins() + "");

                progressXp.setMax(user.getRequiredXp());
                progressXp.setProgress(user.getXp());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(StatsActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEquipment() {
        equipmentRepository.getEquipmentForUser(userId, new EquipmentRepository.OnEquipmentLoaded() {
            @Override
            public void onSuccess(List<Equipment> equipment) {
                if (equipment.isEmpty()) {
                    recyclerStatsEquipment.setVisibility(View.GONE);
                    tvNoEquipmentStats.setVisibility(View.VISIBLE);
                } else {
                    recyclerStatsEquipment.setVisibility(View.VISIBLE);
                    tvNoEquipmentStats.setVisibility(View.GONE);

                    StatsEquipmentAdapter adapter = new StatsEquipmentAdapter(StatsActivity.this, equipment);
                    recyclerStatsEquipment.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(StatsActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBosses() {
        bossRepository.getBossesForUser(userId, new BossRepository.OnBossesLoaded() {
            @Override
            public void onSuccess(List<Boss> bosses) {
                // Prikaži samo pobeđene bosove
                List<Boss> defeatedBosses = new ArrayList<>();
                for (Boss b : bosses) {
                    if (b.isDefeated()) {
                        defeatedBosses.add(b);
                    }
                }

                if (defeatedBosses.isEmpty()) {
                    recyclerStatsBosses.setVisibility(View.GONE);
                    tvNoBossesStats.setVisibility(View.VISIBLE);
                } else {
                    recyclerStatsBosses.setVisibility(View.VISIBLE);
                    tvNoBossesStats.setVisibility(View.GONE);

                    StatsBossAdapter adapter = new StatsBossAdapter(StatsActivity.this, defeatedBosses);
                    recyclerStatsBosses.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(StatsActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}