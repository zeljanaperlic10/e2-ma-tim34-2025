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
import com.example.myapplication.domain.service.TaskService;

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
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Povezi view-ove
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

        // Rekonstruiši Task objekat iz Intent podataka
        currentTask = buildTaskFromIntent();

        // Prikaži podatke
        populateUI();

        // Postavi dugmad
        setupButtons();
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

        return task;
    }

    private void populateUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        // Naziv
        tvDetailName.setText(currentTask.getName());

        // Opis
        String desc = currentTask.getDescription();
        tvDetailDescription.setText((desc != null && !desc.isEmpty()) ? desc : "—");

        // Kategorija
        tvDetailCategory.setText(currentTask.getCategory());

        // Boja kategorije
        viewCategoryColorBar.setBackgroundColor(currentTask.getCategoryColor());

        // Težina i bitnost
        tvDetailDifficulty.setText(currentTask.getDifficultyLabel() +
                " (" + currentTask.getDifficultyXP() + " XP)");
        tvDetailImportance.setText(currentTask.getImportanceLabel() +
                " (" + currentTask.getImportanceXP() + " XP)");

        // Ukupno XP
        tvDetailXP.setText(currentTask.getTotalXP() + " XP");

        // Datum početka
        if (currentTask.getStartDate() != null && currentTask.getStartDate() > 0) {
            tvDetailStartDate.setText(sdf.format(new Date(currentTask.getStartDate())));
        }

        // Datum završetka — samo za ponavljajuće
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

        // Status i boja
        updateStatusUI(currentTask.getStatus());
    }

    private void updateStatusUI(String status) {
        if (status == null) status = "ACTIVE";
        switch (status) {
            case "ACTIVE":
                tvDetailStatus.setText("AKTIVAN");
                tvDetailStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                // Aktivan — prikaži urađen, pauziraj, otkaži
                btnMarkDone.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnActivate.setVisibility(View.GONE);
                btnCancel.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                // Pauza samo za ponavljajuće
                if (currentTask.getFrequencyType() == Task.FrequencyType.ONE_TIME) {
                    btnPause.setVisibility(View.GONE);
                }
                break;
            case "PAUSED":
                tvDetailStatus.setText("PAUZIRAN");
                tvDetailStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                // Pauziran — prikaži aktiviraj
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
                // Urađen — ništa ne može
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
                // Otkazan — ništa ne može
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
                // Neurađen — ništa ne može
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

        // Označi kao urađen
        btnMarkDone.setOnClickListener(v -> {
            taskService.markTaskDone(currentTask, new TaskService.OnTaskOperation() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    currentTask.setStatus("DONE");
                    updateStatusUI("DONE");
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Pauziraj
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

        // Aktiviraj (iz pauziranog)
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

        // Otkaži
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

        // Izmeni
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

        // Obriši
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Obriši zadatak")
                    .setMessage("Da li si sigurna da želiš da obrišeš zadatak?")
                    .setPositiveButton("Da", (dialog, which) -> {
                        taskService.deleteTask(currentTask, new TaskService.OnTaskOperation() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                                finish(); // Vrati se na listu
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
