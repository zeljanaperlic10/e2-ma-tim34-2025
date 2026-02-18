package com.example.myapplication.presentation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Equipment;
import com.example.myapplication.data.repository.EquipmentRepository;
import com.example.myapplication.data.repository.UserRepository;

import java.util.Random;

public class BossVictoryActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView ivChest;
    private TextView tvShakeToOpen, tvCoinsReward, tvEquipmentReward;
    private LinearLayout layoutRewards;
    private Button btnClose;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 15.0f;
    private long lastShakeTime = 0;
    private boolean chestOpened = false;

    private UserRepository userRepository = new UserRepository();
    private EquipmentRepository equipmentRepository = new EquipmentRepository();
    private String userId;
    private int coinsReward;
    private Equipment equipmentReward = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_victory);

        userId = getIntent().getStringExtra("USER_ID");
        coinsReward = getIntent().getIntExtra("COINS_REWARD", 0);

        ivChest = findViewById(R.id.ivChest);
        tvShakeToOpen = findViewById(R.id.tvShakeToOpen);
        tvCoinsReward = findViewById(R.id.tvCoinsReward);
        tvEquipmentReward = findViewById(R.id.tvEquipmentReward);
        layoutRewards = findViewById(R.id.layoutRewards);
        btnClose = findViewById(R.id.btnClose);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 20% šansa za opremu
        Random random = new Random();
        if (random.nextInt(100) < 20) {
            // 95% šansa za odeću, 5% za oružje
            if (random.nextInt(100) < 95) {
                equipmentReward = Equipment.createRandomClothing(userId);
            } else {
                equipmentReward = Equipment.createRandomWeapon(userId);
            }
        }

        btnClose.setOnClickListener(v -> finish());
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

    private void openChest() {
        if (chestOpened) return;
        chestOpened = true;

        // Promeni ikonicu kovčega
        ivChest.setImageResource(android.R.drawable.ic_lock_idle_lock);
        tvShakeToOpen.setVisibility(View.GONE);

        // Prikaži nagrade
        tvCoinsReward.setText("💰 " + coinsReward + " novčića");

        if (equipmentReward != null) {
            tvEquipmentReward.setText("⚔️ " + equipmentReward.getName());
            tvEquipmentReward.setVisibility(View.VISIBLE);

            // Dodaj opremu u bazu
            equipmentRepository.addEquipment(equipmentReward, new EquipmentRepository.OnOperationComplete() {
                @Override
                public void onSuccess() {
                    Toast.makeText(BossVictoryActivity.this, "Dobio si: " + equipmentReward.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(BossVictoryActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        layoutRewards.setVisibility(View.VISIBLE);
        btnClose.setVisibility(View.VISIBLE);

        // Dodaj novčiće korisniku
        userRepository.addCoins(userId, coinsReward, new UserRepository.OnOperationComplete() {
            @Override
            public void onSuccess() {
                Toast.makeText(BossVictoryActivity.this, "Dobio si " + coinsReward + " novčića!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BossVictoryActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            long currentTime = System.currentTimeMillis();
            if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime) > 500 && !chestOpened) {
                lastShakeTime = currentTime;
                openChest();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
