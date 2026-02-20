package com.example.myapplication.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Alliance;
import com.example.myapplication.data.model.ChatMessage;
import com.example.myapplication.data.model.MissionContribution;
import com.example.myapplication.data.repository.AllianceRepository;
import com.example.myapplication.presentation.adapters.ChatAdapter;
import com.example.myapplication.presentation.adapters.MembersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllianceActivity extends AppCompatActivity {

    private TextView allianceNameText, leaderText, membersText;
    private TextView missionStatusText, tvBossHpLabel, tvBossHp;
    private ProgressBar progressBossHp;
    private Button startMissionBtn, leaveAllianceBtn, btnMissionProgress, btnDeleteAlliance;
    private RecyclerView chatRecyclerView, membersRecyclerView;
    private EditText messageInput;
    private Button sendMessageBtn;

    private ChatAdapter chatAdapter;
    private MembersAdapter membersAdapter;
    private AllianceRepository repository;
    private String currentUserId, currentUsername;
    private Alliance currentAlliance;
    private ListenerRegistration messageListener, allianceListener;
    private CountDownTimer missionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance);

        repository = new AllianceRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        bindViews();
        setupAdapters();
        setupButtons();
        loadUserData();
        loadAlliance();
    }

    private void bindViews() {
        allianceNameText   = findViewById(R.id.allianceNameText);
        leaderText         = findViewById(R.id.leaderText);
        membersText        = findViewById(R.id.membersText);
        missionStatusText  = findViewById(R.id.missionStatusText);
        tvBossHpLabel      = findViewById(R.id.tvBossHpLabel);
        tvBossHp           = findViewById(R.id.tvBossHp);
        progressBossHp     = findViewById(R.id.progressBossHp);
        chatRecyclerView   = findViewById(R.id.chatRecyclerView);
        membersRecyclerView= findViewById(R.id.membersRecyclerView);
        messageInput       = findViewById(R.id.messageInput);
        sendMessageBtn     = findViewById(R.id.sendMessageBtn);
        leaveAllianceBtn   = findViewById(R.id.leaveAllianceBtn);
        startMissionBtn    = findViewById(R.id.startMissionBtn);
        btnMissionProgress = findViewById(R.id.btnMissionProgress);
        btnDeleteAlliance  = findViewById(R.id.btnDeleteAlliance);
    }

    private void setupAdapters() {
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUserId);
        chatRecyclerView.setAdapter(chatAdapter);

        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MembersAdapter(new ArrayList<>());
        membersRecyclerView.setAdapter(membersAdapter);
    }

    private void setupButtons() {
        sendMessageBtn.setOnClickListener(v -> sendMessage());
        leaveAllianceBtn.setOnClickListener(v -> leaveAlliance());
        startMissionBtn.setOnClickListener(v -> confirmStartMission());
        btnMissionProgress.setOnClickListener(v -> openMissionProgress());
        btnDeleteAlliance.setOnClickListener(v -> confirmDeleteAlliance());
    }

    private void loadUserData() {
        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> currentUsername = doc.getString("username"));
    }

    private void loadAlliance() {
        repository.loadCurrentAlliance(currentUserId, new AllianceRepository.OnAllianceLoaded() {
            @Override
            public void onAllianceLoaded(Alliance alliance) {
                if (alliance == null) {
                    Toast.makeText(AllianceActivity.this, "Nisi u savezu", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentAlliance = alliance;
                displayAllianceInfo(alliance);
                listenToMessages(alliance.getId());
                listenToAllianceChanges(alliance.getId());
                checkMissionStatus(alliance);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(AllianceActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAllianceInfo(Alliance alliance) {
        allianceNameText.setText(alliance.getName());
        leaderText.setText("Vođa: " + alliance.getLeaderUsername());
        List<String> members = alliance.getMemberUsernames();
        if (members != null && !members.isEmpty()) {
            membersText.setText("Članovi (" + members.size() + "): " + String.join(", ", members));
            membersAdapter.updateMembers(members);
        }
        boolean isLeader = alliance.getLeaderId().equals(currentUserId);
        startMissionBtn.setVisibility(isLeader ? View.VISIBLE : View.GONE);
        btnDeleteAlliance.setVisibility(isLeader ? View.VISIBLE : View.GONE);
        leaveAllianceBtn.setVisibility(isLeader ? View.GONE : View.VISIBLE);
    }

    private void listenToAllianceChanges(String allianceId) {
        if (allianceListener != null) allianceListener.remove();
        allianceListener = FirebaseFirestore.getInstance()
                .collection("alliances").document(allianceId)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null || !snap.exists()) return;
                    Boolean active = snap.getBoolean("missionActive");
                    Long maxHp = snap.getLong("missionBossMaxHp");
                    Long curHp = snap.getLong("missionBossCurrentHp");
                    if (active != null && active && maxHp != null && curHp != null) {
                        tvBossHpLabel.setVisibility(View.VISIBLE);
                        tvBossHp.setVisibility(View.VISIBLE);
                        progressBossHp.setVisibility(View.VISIBLE);
                        tvBossHp.setText(curHp + " / " + maxHp + " HP");
                        progressBossHp.setMax(maxHp.intValue());
                        progressBossHp.setProgress(curHp.intValue());
                        btnMissionProgress.setVisibility(View.VISIBLE);
                    } else {
                        tvBossHpLabel.setVisibility(View.GONE);
                        tvBossHp.setVisibility(View.GONE);
                        progressBossHp.setVisibility(View.GONE);
                        btnMissionProgress.setVisibility(View.GONE);
                    }
                });
    }

    private void checkMissionStatus(Alliance alliance) {
        if (alliance.isMissionActive()) {
            long remaining = alliance.getMissionEndTime() - System.currentTimeMillis();
            if (remaining > 0) {
                startMissionBtn.setEnabled(false);
                startMissionCountdown(remaining);
            } else {
                handleMissionExpired(alliance.getId());
            }
        } else {
            missionStatusText.setText("Nema aktivne misije");
            startMissionBtn.setEnabled(true);
        }
    }

    private void handleMissionExpired(String allianceId) {
        repository.finishMission(allianceId, new AllianceRepository.OnMissionFinished() {
            @Override
            public void onFinished(boolean won, List<MissionContribution> contributions) {
                String result = won ? "Savez je pobedio bosa!" : "Bos nije poražen.";
                missionStatusText.setText(result + " (Misija završena)");
                startMissionBtn.setEnabled(true);
                if (won) showMissionRewardDialog();
            }
            @Override
            public void onError(String message) {
                missionStatusText.setText("Misija završena.");
                startMissionBtn.setEnabled(true);
            }
        });
    }

    private void confirmStartMission() {
        if (currentAlliance == null) return;
        int memberCount = currentAlliance.getMemberIds() != null ? currentAlliance.getMemberIds().size() : 1;
        int bossHp = 100 * memberCount;
        new AlertDialog.Builder(this)
                .setTitle("Pokreni specijalnu misiju")
                .setMessage("Bos: " + bossHp + " HP (" + memberCount + " članova × 100)\nTrajanje: 2 minuta (demo). Pokreni?")
                .setPositiveButton("Pokreni", (d, w) -> startMission(memberCount))
                .setNegativeButton("Otkaži", null).show();
    }

    private void startMission(int memberCount) {
        if (currentAlliance == null) return;
        repository.startMissionWithBoss(currentAlliance.getId(), currentUserId, memberCount,
                new AllianceRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AllianceActivity.this, "Misija pokrenuta!", Toast.LENGTH_SHORT).show();
                        startMissionBtn.setEnabled(false);
                        startMissionCountdown(2L * 60 * 1000);
                        btnMissionProgress.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(AllianceActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMissionCountdown(long milliseconds) {
        if (missionTimer != null) missionTimer.cancel();
        missionTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long ms) {
                missionStatusText.setText("Misija aktivna: " + ms / 1000 + "s");
            }
            @Override
            public void onFinish() {
                missionStatusText.setText("Misija istekla! Računam rezultat...");
                if (currentAlliance != null) handleMissionExpired(currentAlliance.getId());
            }
        };
        missionTimer.start();
    }

    private void openMissionProgress() {
        if (currentAlliance == null) return;
        Intent intent = new Intent(this, MissionProgressActivity.class);
        intent.putExtra("ALLIANCE_ID", currentAlliance.getId());
        startActivity(intent);
    }

    private void showMissionRewardDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Savez je pobedio!")
                .setMessage("Svaki član dobija:\n• 1 napitak\n• 1 komad odeće\n• 50% novčića od nagrade narednog bosa\n\nNagrade su dodate.")
                .setPositiveButton("OK", null).show();
    }

    private void listenToMessages(String allianceId) {
        if (messageListener != null) messageListener.remove();
        messageListener = repository.listenToMessages(allianceId, new AllianceRepository.OnMessagesLoaded() {
            @Override
            public void onMessagesLoaded(List<ChatMessage> messages) {
                chatAdapter.updateMessages(messages);
                if (!messages.isEmpty())
                    chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(AllianceActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String msg = messageInput.getText().toString().trim();
        if (msg.isEmpty()) { Toast.makeText(this, "Unesi poruku!", Toast.LENGTH_SHORT).show(); return; }
        if (currentAlliance == null) return;

        repository.sendMessage(currentAlliance.getId(), currentUserId, currentUsername, msg,
                new AllianceRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() {
                        messageInput.setText("");
                        // Misijski doprinos: 4 HP po danu
                        if (currentAlliance.isMissionActive()) {
                            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                            repository.registerMessageDay(currentAlliance.getId(), currentUserId, currentUsername, today,
                                    new AllianceRepository.OnHpReduced() {
                                        @Override public void onSuccess(int hp, int newBossHp) {}
                                        @Override public void onAlreadyMaxed() {}
                                        @Override public void onError(String e) {}
                                    });
                        }
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(AllianceActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void leaveAlliance() {
        if (currentAlliance == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Napusti savez")
                .setMessage("Sigurno želiš napustiti savez?")
                .setPositiveButton("Da", (d, w) ->
                        repository.leaveAlliance(currentAlliance.getId(), currentUserId,
                                new AllianceRepository.OnOperationComplete() {
                                    @Override public void onSuccess() { Toast.makeText(AllianceActivity.this, "Napustio si savez", Toast.LENGTH_SHORT).show(); finish(); }
                                    @Override public void onError(String msg) { Toast.makeText(AllianceActivity.this, msg, Toast.LENGTH_SHORT).show(); }
                                }))
                .setNegativeButton("Ne", null).show();
    }

    private void confirmDeleteAlliance() {
        if (currentAlliance == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Ukini savez")
                .setMessage("Ovo će rasformirati savez i izbaciti sve članove. Nastaviti?")
                .setPositiveButton("Ukini", (d, w) ->
                        repository.deleteAlliance(currentAlliance.getId(), currentUserId,
                                new AllianceRepository.OnOperationComplete() {
                                    @Override public void onSuccess() { Toast.makeText(AllianceActivity.this, "Savez ukinut", Toast.LENGTH_SHORT).show(); finish(); }
                                    @Override public void onError(String msg) { Toast.makeText(AllianceActivity.this, msg, Toast.LENGTH_SHORT).show(); }
                                }))
                .setNegativeButton("Ne", null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) messageListener.remove();
        if (allianceListener != null) allianceListener.remove();
        if (missionTimer != null) missionTimer.cancel();
    }
}