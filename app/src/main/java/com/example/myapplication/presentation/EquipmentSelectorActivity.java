package com.example.myapplication.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Equipment;
import com.example.myapplication.data.repository.EquipmentRepository;
import com.example.myapplication.presentation.adapters.EquipmentAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class EquipmentSelectorActivity extends AppCompatActivity {

    private RecyclerView recyclerEquipment;
    private TextView tvNoEquipment;
    private Button btnStartBattle;

    private EquipmentAdapter equipmentAdapter;
    private EquipmentRepository equipmentRepository = new EquipmentRepository();
    private String userId;
    private List<Equipment> allEquipment = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_selector);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerEquipment = findViewById(R.id.recyclerEquipment);
        tvNoEquipment = findViewById(R.id.tvNoEquipment);
        btnStartBattle = findViewById(R.id.btnStartBattle);

        recyclerEquipment.setLayoutManager(new LinearLayoutManager(this));
        equipmentAdapter = new EquipmentAdapter(this, allEquipment);
        recyclerEquipment.setAdapter(equipmentAdapter);

        btnStartBattle.setOnClickListener(v -> {
            List<Equipment> selectedEquipment = equipmentAdapter.getSelectedEquipment();

            Intent intent = new Intent(this, BossFightActivity.class);
            ArrayList<String> selectedIds = new ArrayList<>();
            for (Equipment eq : selectedEquipment) {
                selectedIds.add(eq.getFirestoreId());
            }
            intent.putStringArrayListExtra("SELECTED_EQUIPMENT_IDS", selectedIds);
            startActivity(intent);
            finish();
        });

        loadEquipment();
    }

    private void loadEquipment() {
        equipmentRepository.getEquipmentForUser(userId, new EquipmentRepository.OnEquipmentLoaded() {
            @Override
            public void onSuccess(List<Equipment> equipment) {
                // Filtriraj samo opremu koja ima remaining battles > 0 ili je weapon
                allEquipment.clear();
                for (Equipment eq : equipment) {
                    if (eq.getType().equals("WEAPON") || eq.getRemainingBattles() > 0) {
                        allEquipment.add(eq);
                    }
                }

                equipmentAdapter.updateList(allEquipment);

                if (allEquipment.isEmpty()) {
                    recyclerEquipment.setVisibility(View.GONE);
                    tvNoEquipment.setVisibility(View.VISIBLE);
                } else {
                    recyclerEquipment.setVisibility(View.VISIBLE);
                    tvNoEquipment.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EquipmentSelectorActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
