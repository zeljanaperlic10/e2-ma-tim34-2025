package com.example.myapplication.presentation;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Boss;
import com.example.myapplication.data.model.Equipment;
import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.repository.BossRepository;
import com.example.myapplication.data.repository.EquipmentRepository;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.util.TaskSuccessCalculator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossFightActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvBossLevel, tvBossHp, tvPlayerPp, tvSuccessChance, tvAttacksRemaining, tvBattleLog;
    private ProgressBar progressBossHp;
    private ImageView ivBossImage;
    private Button btnAttack;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 15.0f;
    private long lastShakeTime = 0;

    private BossRepository bossRepository = new BossRepository();
    private UserRepository userRepository = new UserRepository();
    private TaskRepository taskRepository = new TaskRepository();
    private EquipmentRepository equipmentRepository = new EquipmentRepository();
    private String userId;

    private Boss currentBoss;
    private User currentUser;
    private int attacksRemaining = 5;
    private int successChancePercent = 0;

    // Oprema
    private List<Equipment> selectedEquipment = new ArrayList<>();
    private int bonusPp = 0;
    private int bonusSuccessChance = 0;
    private int bonusAttacks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvBossLevel = findViewById(R.id.tvBossLevel);
        tvBossHp = findViewById(R.id.tvBossHp);
        tvPlayerPp = findViewById(R.id.tvPlayerPp);
        tvSuccessChance = findViewById(R.id.tvSuccessChance);
        tvAttacksRemaining = findViewById(R.id.tvAttacksRemaining);
        tvBattleLog = findViewById(R.id.tvBattleLog);
        progressBossHp = findViewById(R.id.progressBossHp);
        ivBossImage = findViewById(R.id.ivBossImage);
        btnAttack = findViewById(R.id.btnAttack);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        btnAttack.setOnClickListener(v -> performAttack());

        loadUserAndBoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void loadSelectedEquipment() {
        ArrayList<String> selectedIds = getIntent().getStringArrayListExtra("SELECTED_EQUIPMENT_IDS");
        if (selectedIds == null || selectedIds.isEmpty()) {
            return;
        }

        equipmentRepository.getEquipmentForUser(userId, new EquipmentRepository.OnEquipmentLoaded() {
            @Override
            public void onSuccess(List<Equipment> allEquipment) {
                for (Equipment eq : allEquipment) {
                    if (selectedIds.contains(eq.getFirestoreId())) {
                        selectedEquipment.add(eq);
                        bonusPp += eq.getBonusPp();
                        bonusSuccessChance += eq.getBonusSuccessChance();
                        bonusAttacks += eq.getBonusAttacks();
                    }
                }
                attacksRemaining = 5 + bonusAttacks;
                updateUI();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BossFightActivity.this, "Greška pri učitavanju opreme: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserAndBoss() {
        userRepository.getUser(userId, new UserRepository.OnUserLoaded() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                loadSelectedEquipment();
                tvPlayerPp.setText((user.getPp() + bonusPp) + " PP");

                loadTasksAndCalculateSuccess();
                loadOrCreateBoss(user.getLevel() + 1);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BossFightActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasksAndCalculateSuccess() {
        taskRepository.getTasksForUser(userId, new TaskRepository.OnTasksLoaded() {
            @Override
            public void onSuccess(List<Task> tasks) {
                // Računaj % samo za trenutnu etapu
                long etapaStart = currentUser.getLastLevelUpTimestamp();
                long etapaEnd = System.currentTimeMillis();
                successChancePercent = TaskSuccessCalculator.calculateSuccessPercentForPeriod(tasks, etapaStart, etapaEnd);
                successChancePercent += bonusSuccessChance;
                if (successChancePercent == 0) {
                    successChancePercent = 50;
                }
                if (successChancePercent > 100) {
                    successChancePercent = 100;
                }
                tvSuccessChance.setText("Šansa napada: " + successChancePercent + "%");
            }

            @Override
            public void onError(String message) {
                successChancePercent = 50 + bonusSuccessChance;
                tvSuccessChance.setText("Šansa napada: " + successChancePercent + "%");
            }
        });
    }

    private void loadOrCreateBoss(int level) {
        bossRepository.getBossesForUser(userId, new BossRepository.OnBossesLoaded() {
            @Override
            public void onSuccess(List<Boss> bosses) {
                Boss boss = null;
                for (Boss b : bosses) {
                    if (b.getLevel() == level && !b.isDefeated()) {
                        boss = b;
                        break;
                    }
                }

                if (boss == null) {
                    int maxHp = Boss.calculateHpForLevel(level);
                    boss = new Boss(level, maxHp, userId);
                    Boss finalBoss = boss;
                    bossRepository.createBoss(boss, new BossRepository.OnOperationComplete() {
                        @Override
                        public void onSuccess() {
                            currentBoss = finalBoss;
                            updateUI();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(BossFightActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    currentBoss = boss;
                    updateUI();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BossFightActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (currentBoss == null) return;

        tvBossLevel.setText("Boss Nivo " + currentBoss.getLevel());
        tvBossHp.setText(currentBoss.getHp() + " / " + currentBoss.getMaxHp());
        progressBossHp.setMax(currentBoss.getMaxHp());
        progressBossHp.setProgress(currentBoss.getHp());
        tvAttacksRemaining.setText("Preostalo napada: " + attacksRemaining + " / " + (5 + bonusAttacks));
    }

    private void performAttack() {
        if (attacksRemaining <= 0) {
            endBattle();
            return;
        }

        attacksRemaining--;

        Random random = new Random();
        int roll = random.nextInt(100);

        if (roll < successChancePercent) {
            int damage = currentUser.getPp() + bonusPp;
            currentBoss.setHp(Math.max(0, currentBoss.getHp() - damage));
            tvBattleLog.setText("💥 Pogodak! Naneto " + damage + " štete!");
        } else {
            tvBattleLog.setText("❌ Promašaj!");
        }

        updateUI();

        if (currentBoss.getHp() <= 0) {
            currentBoss.setDefeated(true);
            bossRepository.updateBoss(currentBoss, new BossRepository.OnOperationComplete() {
                @Override
                public void onSuccess() {
                    decreaseEquipmentDurability();
                    endBattle();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(BossFightActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (attacksRemaining <= 0) {
            decreaseEquipmentDurability();
            endBattle();
        }
    }

    private void decreaseEquipmentDurability() {
        for (Equipment eq : selectedEquipment) {
            if (eq.getType().equals("CLOTHING")) {
                eq.setRemainingBattles(eq.getRemainingBattles() - 1);
                if (eq.getRemainingBattles() <= 0) {
                    equipmentRepository.deleteEquipment(eq.getFirestoreId(), new EquipmentRepository.OnOperationComplete() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError(String message) {}
                    });
                } else {
                    equipmentRepository.updateEquipment(eq, new EquipmentRepository.OnOperationComplete() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError(String message) {}
                    });
                }
            }
        }
    }

    private void endBattle() {
        if (currentBoss.isDefeated()) {
            int coinsReward = calculateCoinsReward(currentBoss.getLevel());

            Intent intent = new Intent(this, BossVictoryActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("COINS_REWARD", coinsReward);
            startActivity(intent);
            finish();

        } else {
            int damagePercent = (int) (((float) (currentBoss.getMaxHp() - currentBoss.getHp()) / currentBoss.getMaxHp()) * 100);
            if (damagePercent >= 50) {
                int halfReward = calculateCoinsReward(currentBoss.getLevel()) / 2;
                userRepository.addCoins(userId, halfReward, new UserRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() {
                        showPartialRewardDialog(halfReward);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(BossFightActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                showDefeatDialog();
            }
        }
    }

    private int calculateCoinsReward(int level) {
        if (level == 1) return 200;
        return (int) (calculateCoinsReward(level - 1) * 1.2);
    }

    private void showPartialRewardDialog(int coins) {
        new AlertDialog.Builder(this)
                .setTitle("⚔️ Delimična pobeda")
                .setMessage("Smanjio si bosa 50%+!\nDobio si " + coins + " novčića!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showDefeatDialog() {
        new AlertDialog.Builder(this)
                .setTitle("💀 Poraz")
                .setMessage("Bos te pobedio! Pokušaj ponovo!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            long currentTime = System.currentTimeMillis();
            if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime) > 500) {
                lastShakeTime = currentTime;
                performAttack();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}