package com.example.myapplication.presentation;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Alliance;
import com.example.myapplication.data.model.ChatMessage;
import com.example.myapplication.data.repository.AllianceRepository;
import com.example.myapplication.presentation.adapters.ChatAdapter;
import com.example.myapplication.presentation.adapters.MembersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AllianceActivity extends AppCompatActivity {
    private TextView allianceNameText, leaderText, membersText, missionStatusText;
    private RecyclerView chatRecyclerView, membersRecyclerView;
    private EditText messageInput;
    private Button sendMessageBtn, leaveAllianceBtn, startMissionBtn;
    private ChatAdapter chatAdapter;
    private MembersAdapter membersAdapter;
    private AllianceRepository repository;
    private String currentUserId;
    private String currentUsername;
    private Alliance currentAlliance;
    private ListenerRegistration messageListener;
    private CountDownTimer missionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance);

        repository = new AllianceRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        allianceNameText = findViewById(R.id.allianceNameText);
        leaderText = findViewById(R.id.leaderText);
        membersText = findViewById(R.id.membersText);
        missionStatusText = findViewById(R.id.missionStatusText);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        leaveAllianceBtn = findViewById(R.id.leaveAllianceBtn);
        startMissionBtn = findViewById(R.id.startMissionBtn);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUserId);
        chatRecyclerView.setAdapter(chatAdapter);

        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MembersAdapter(new ArrayList<>());
        membersRecyclerView.setAdapter(membersAdapter);

        sendMessageBtn.setOnClickListener(v -> sendMessage());
        leaveAllianceBtn.setOnClickListener(v -> leaveAlliance());
        startMissionBtn.setOnClickListener(v -> startMission());

        loadUserData();
        loadAlliance();
    }

    private void loadUserData() {
        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    currentUsername = doc.getString("username");
                });
    }

    private void loadAlliance() {
        repository.loadCurrentAlliance(currentUserId, new AllianceRepository.OnAllianceLoaded() {
            @Override
            public void onAllianceLoaded(Alliance alliance) {
                if (alliance != null) {
                    currentAlliance = alliance;
                    displayAllianceInfo(alliance);
                    listenToMessages(alliance.getId());
                    checkMissionStatus(alliance);
                } else {
                    Toast.makeText(AllianceActivity.this, "Nisi u savezu", Toast.LENGTH_SHORT).show();
                    finish();
                }
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

        StringBuilder members = new StringBuilder("Članovi: ");
        for (String member : alliance.getMemberUsernames()) {
            members.append(member).append(", ");
        }
        if (members.length() > 10) {
            members.setLength(members.length() - 2);
        }
        membersText.setText(members.toString());

        membersAdapter.updateMembers(alliance.getMemberUsernames());

        // Prikaži dugme za pokretanje misije samo za vođu
        if (alliance.getLeaderId().equals(currentUserId)) {
            startMissionBtn.setVisibility(View.VISIBLE);
            leaveAllianceBtn.setVisibility(View.GONE);
        } else {
            startMissionBtn.setVisibility(View.GONE);
            leaveAllianceBtn.setVisibility(View.VISIBLE);
        }
    }

    private void checkMissionStatus(Alliance alliance) {
        if (alliance.isMissionActive()) {
            long remainingTime = alliance.getMissionEndTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                startMissionCountdown(remainingTime);
            } else {
                // Misija je završena
                FirebaseFirestore.getInstance().collection("alliances")
                        .document(alliance.getId())
                        .update("missionActive", false, "missionEndTime", 0);
            }
        } else {
            missionStatusText.setText("Nema aktivne misije");
            startMissionBtn.setEnabled(true);
        }
    }

    private void listenToMessages(String allianceId) {
        messageListener = repository.listenToMessages(allianceId,
                new AllianceRepository.OnMessagesLoaded() {
                    @Override
                    public void onMessagesLoaded(List<ChatMessage> messages) {
                        chatAdapter.updateMessages(messages);
                        if (!messages.isEmpty()) {
                            chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AllianceActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Unesi poruku!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentAlliance == null) return;

        repository.sendMessage(currentAlliance.getId(), currentUserId, currentUsername, message,
                new AllianceRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() {
                        messageInput.setText("");
                    }

                    @Override
                    public void onError(String msg) {
                        Toast.makeText(AllianceActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void leaveAlliance() {
        if (currentAlliance == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Napusti savez")
                .setMessage("Da li si siguran da želiš napustiti savez?")
                .setPositiveButton("Da", (dialog, which) -> {
                    repository.leaveAlliance(currentAlliance.getId(), currentUserId,
                            new AllianceRepository.OnOperationComplete() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(AllianceActivity.this, "Napustio si savez", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(AllianceActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Ne", null)
                .show();
    }

    private void startMission() {
        if (currentAlliance == null) return;

        repository.startMission(currentAlliance.getId(), currentUserId,
                new AllianceRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AllianceActivity.this, "Misija pokrenuta!", Toast.LENGTH_SHORT).show();
                        startMissionCountdown(60000); // 1 minut
                        startMissionBtn.setEnabled(false);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AllianceActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMissionCountdown(long milliseconds) {
        if (missionTimer != null) {
            missionTimer.cancel();
        }

        missionTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                missionStatusText.setText("Misija aktivna: " + seconds + "s");
            }

            @Override
            public void onFinish() {
                missionStatusText.setText("Misija završena!");
                if (currentAlliance != null) {
                    FirebaseFirestore.getInstance().collection("alliances")
                            .document(currentAlliance.getId())
                            .update("missionActive", false, "missionEndTime", 0);
                }
                startMissionBtn.setEnabled(true);
            }
        };
        missionTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
        if (missionTimer != null) {
            missionTimer.cancel();
        }
    }
}
