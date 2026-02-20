package com.example.myapplication.presentation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.repository.AllianceRepository;
import com.example.myapplication.domain.service.TaskService;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView tvDetailName, tvDetailStatus, tvDetailDescription,
            tvDetailCategory, tvDetailDifficulty, tvDetailImportance,
            tvDetailXP, tvDetailStartDate, tvDetailEndDate,
            tvDetailFrequency, labelEndDate;
    private View viewCategoryColorBar;
    private Button btnMarkDone, btnPause, btnActivate, btnCancel, btnEdit, btnDelete;

    private TaskService taskService = new TaskService();
    private AllianceRepository allianceRepository = new AllianceRepository();
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailDifficulty = findViewById(R.id.tvDetailDifficulty);
        tvDetailImportance = findViewById(R.id.tvDetailImportance);
        tvDetailXP = findViewById(R.id.tvDetailXP);
        tvDetailStartDate = findViewById(R.id.tvDetailStartDate);
        tvDetailEndDate = findViewById(R.id.tvDetailEndDate);
        tvDetailFrequency = findViewById(R.id.tvDetailFrequency);
        labelEndDate = findViewById(R.id.labelEndDate);
        viewCategoryColorBar = findViewById(R.id.viewCategoryColorBar);
        btnMarkDone = findViewById(R.id.btnMarkDone);
        btnPause = findViewById(R.id.btnPause);
        btnActivate = findViewById(R.id.btnActivate);
        btnCancel = findViewById(R.id.btnCancel);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        currentTask = buildTaskFromIntent();
        populateUI();
        setupButtons();
    }

    /**
     * Registruje rešen zadatak kao doprinos u specijalnoj misiji saveza (ako postoji).
     */
    private void registerTaskInMission(Task task) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String allianceId = doc.getString("currentAllianceId");
                    String uname = doc.getString("username");
                    if (allianceId == null || allianceId.isEmpty()) return;

                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("alliances").document(allianceId).get()
                            .addOnSuccessListener(aDoc -> {
                                Boolean active = aDoc.getBoolean("missionActive");
                                if (active == null || !active) return;

                                String username = uname != null ? uname : "Korisnik";
                                allianceRepository.registerTaskCompleted(
                                        allianceId, uid, username,
                                        task.getDifficultyXP(), task.getImportanceXP(),
                                        new com.example.myapplication.data.repository.AllianceRepository.OnHpReduced() {
                                            @Override public void onSuccess(int hp, int newHp) {}
                                            @Override public void onAlreadyMaxed() {}
                                            @Override public void onError(String e) {}
                                        });
                            });
                });
    }

    private Task buildTaskFromIntent() {
        Task task = new Task();
        task.setFirestoreId(getIntent().getStringExtra("FIRESTORE_ID"));
        task.setName(getIntent().getStringExtra("TASK_NAME"));
        task.setDescription(getIntent().getStringExtra("TASK_DESCRIPTION"));
        task.setCategory(getIntent().getStringExtra("TASK_CATEGORY"));
        task.setCategoryColor(getIntent().getIntExtra("TASK_CATEGORY_COLOR", Color.BLUE));
        task.setDifficultyXP(getIntent().getIntExtra("TASK_DIFFICULTY", 1));
        task.setImportanceXP(getIntent().getIntExtra("TASK_IMPORTANCE", 1));
        task.setTotalXP(getIntent().getIntExtra("TASK_TOTAL_XP", 2));
        task.setStatus(getIntent().getStringExtra("TASK_STATUS"));
        task.setStartDate(getIntent().getLongExtra("TASK_START_DATE", 0L));
        long endDate = getIntent().getLongExtra("TASK_END_DATE", 0L);
        task.setEndDate(endDate > 0 ? endDate : null);

        String freq = getIntent().getStringExtra("TASK_FREQUENCY");
        task.setFrequencyType(freq != null && freq.equals("REPEATING") ?
                Task.FrequencyType.REPEATING : Task.FrequencyType.ONE_TIME);

        // KRITIČNO: Dodaj userId
        task.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        return task;
    }

    private void populateUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        tvDetailName.setText(currentTask.getName());

        String desc = currentTask.getDescription();
        tvDetailDescription.setText((desc != null && !desc.isEmpty()) ? desc : "—");

        tvDetailCategory.setText(currentTask.getCategory());
        viewCategoryColorBar.setBackgroundColor(currentTask.getCategoryColor());

        tvDetailDifficulty.setText(currentTask.getDifficultyLabel() +
                " (" + currentTask.getDifficultyXP() + " XP)");
        tvDetailImportance.setText(currentTask.getImportanceLabel() +
                " (" + currentTask.getImportanceXP() + " XP)");

        tvDetailXP.setText(currentTask.getTotalXP() + " XP");

        if (currentTask.getStartDate() != null && currentTask.getStartDate() > 0) {
            tvDetailStartDate.setText(sdf.format(new Date(currentTask.getStartDate())));
        }

        if (currentTask.getFrequencyType() == Task.FrequencyType.REPEATING) {
            labelEndDate.setVisibility(View.VISIBLE);
            tvDetailEndDate.setVisibility(View.VISIBLE);
            if (currentTask.getEndDate() != null) {
                tvDetailEndDate.setText(sdf.format(new Date(currentTask.getEndDate())));
            }
            tvDetailFrequency.setText("Ponavljajući");
        } else {
            tvDetailFrequency.setText("Jednokratni");
        }

        updateStatusUI(currentTask.getStatus());
    }

    private void updateStatusUI(String status) {
        if (status == null) status = "ACTIVE";
        switch (status) {
            case "ACTIVE":
                tvDetailStatus.setText("AKTIVAN");
                tvDetailStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                btnMarkDone.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnActivate.setVisibility(View.GONE);
                btnCancel.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                if (currentTask.getFrequencyType() == Task.FrequencyType.ONE_TIME) {
                    btnPause.setVisibility(View.GONE);
                }
                break;
            case "PAUSED":
                tvDetailStatus.setText("PAUZIRAN");
                tvDetailStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                btnMarkDone.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnActivate.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                break;
            case "DONE":
                tvDetailStatus.setText("URAĐEN");
                tvDetailStatus.setBackgroundColor(Color.parseColor("#2196F3"));
                btnMarkDone.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnActivate.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                break;
            case "CANCELLED":
                tvDetailStatus.setText("OTKAZAN");
                tvDetailStatus.setBackgroundColor(Color.parseColor("#F44336"));
                btnMarkDone.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnActivate.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                break;
            case "UNDONE":
                tvDetailStatus.setText("NEURAĐEN");
                tvDetailStatus.setBackgroundColor(Color.parseColor("#9E9E9E"));
                btnMarkDone.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnActivate.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                break;
        }
    }

    private void setupButtons() {

        btnMarkDone.setOnClickListener(v -> {
            android.util.Log.d("TASK_DETAIL", "Mark Done clicked!");
            android.util.Log.d("TASK_DETAIL", "Task: " + currentTask.getName() + ", XP: " + currentTask.getTotalXP());
            android.util.Log.d("TASK_DETAIL", "UserId: " + currentTask.getUserId());

            taskService.markTaskDone(currentTask, new TaskService.OnTaskOperation() {
                @Override
                public void onSuccess(String message) {
                    android.util.Log.d("TASK_DETAIL", "Success: " + message);
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    currentTask.setStatus("DONE");
                    updateStatusUI("DONE");
                    // Registruj rešen zadatak za specijalnu misiju saveza
                    registerTaskInMission(currentTask);
                }

                @Override
                public void onLevelUp(int oldLevel, int newLevel, int oldPp, int newPp, String newTitle, int newRequiredXp) {
                    android.util.Log.d("TASK_DETAIL", "LEVEL UP TRIGGERED!");
                    Toast.makeText(TaskDetailActivity.this, "LEVEL UP!", Toast.LENGTH_SHORT).show();
                    currentTask.setStatus("DONE");
                    updateStatusUI("DONE");

                    Intent intent = new Intent(TaskDetailActivity.this, LevelUpActivity.class);
                    intent.putExtra("NEW_LEVEL", newLevel);
                    intent.putExtra("NEW_TITLE", newTitle);
                    intent.putExtra("OLD_PP", oldPp);
                    intent.putExtra("NEW_PP", newPp);
                    intent.putExtra("NEW_REQUIRED_XP", newRequiredXp);
                    startActivity(intent);
                }

                @Override
                public void onError(String message) {
                    android.util.Log.e("TASK_DETAIL", "Error: " + message);
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnPause.setOnClickListener(v -> {
            taskService.pauseTask(currentTask, new TaskService.OnTaskOperation() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    currentTask.setStatus("PAUSED");
                    updateStatusUI("PAUSED");
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnActivate.setOnClickListener(v -> {
            taskService.activateTask(currentTask, new TaskService.OnTaskOperation() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    currentTask.setStatus("ACTIVE");
                    updateStatusUI("ACTIVE");
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Otkaži zadatak")
                    .setMessage("Da li si sigurna da želiš da otkažeš zadatak?")
                    .setPositiveButton("Da", (dialog, which) -> {
                        taskService.markTaskCancelled(currentTask, new TaskService.OnTaskOperation() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                                currentTask.setStatus("CANCELLED");
                                updateStatusUI("CANCELLED");
                            }
                            @Override
                            public void onError(String message) {
                                Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Ne", null)
                    .show();
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTaskActivity.class);
            intent.putExtra("FIRESTORE_ID", currentTask.getFirestoreId());
            intent.putExtra("TASK_NAME", currentTask.getName());
            intent.putExtra("TASK_DESCRIPTION", currentTask.getDescription());
            intent.putExtra("TASK_DIFFICULTY", currentTask.getDifficultyXP());
            intent.putExtra("TASK_IMPORTANCE", currentTask.getImportanceXP());
            intent.putExtra("TASK_START_DATE", currentTask.getStartDate());
            intent.putExtra("TASK_FREQUENCY", currentTask.getFrequencyType().name());
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Obriši zadatak")
                    .setMessage("Da li si sigurna da želiš da obrišeš zadatak?")
                    .setPositiveButton("Da", (dialog, which) -> {
                        taskService.deleteTask(currentTask, new TaskService.OnTaskOperation() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            @Override
                            public void onError(String message) {
                                Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Ne", null)
                    .show();
        });
    }
}
