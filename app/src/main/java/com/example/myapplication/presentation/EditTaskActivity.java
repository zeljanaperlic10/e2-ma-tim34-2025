package com.example.myapplication.presentation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Task;
import com.example.myapplication.domain.service.TaskService;

import java.util.Calendar;

public class EditTaskActivity extends AppCompatActivity {

    private EditText editTaskName, editTaskDescription;
    private Spinner editSpinnerDifficulty, editSpinnerImportance;
    private Button btnEditSelectDate, btnSaveTask;

    private TaskService taskService = new TaskService();
    private String firestoreId;
    private long selectedStartDate;
    private String taskFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        editTaskName = findViewById(R.id.editTaskName);
        editTaskDescription = findViewById(R.id.editTaskDescription);
        editSpinnerDifficulty = findViewById(R.id.editSpinnerDifficulty);
        editSpinnerImportance = findViewById(R.id.editSpinnerImportance);
        btnEditSelectDate = findViewById(R.id.btnEditSelectDate);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        String[] difficulties = {"Veoma lak", "Lak", "Težak", "Ekstremno težak"};
        editSpinnerDifficulty.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, difficulties));

        String[] importances = {"Normalan", "Važan", "Ekstremno važan", "Specijalan"};
        editSpinnerImportance.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, importances));

        firestoreId = getIntent().getStringExtra("FIRESTORE_ID");
        String name = getIntent().getStringExtra("TASK_NAME");
        String description = getIntent().getStringExtra("TASK_DESCRIPTION");
        int difficulty = getIntent().getIntExtra("TASK_DIFFICULTY", 1);
        int importance = getIntent().getIntExtra("TASK_IMPORTANCE", 1);
        selectedStartDate = getIntent().getLongExtra("TASK_START_DATE", System.currentTimeMillis());
        taskFrequency = getIntent().getStringExtra("TASK_FREQUENCY");

        editTaskName.setText(name);
        editTaskDescription.setText(description);

        int diffPos = 0;
        switch (difficulty) {
            case 1: diffPos = 0; break;
            case 3: diffPos = 1; break;
            case 7: diffPos = 2; break;
            case 20: diffPos = 3; break;
        }
        editSpinnerDifficulty.setSelection(diffPos);

        int impPos = 0;
        switch (importance) {
            case 1: impPos = 0; break;
            case 3: impPos = 1; break;
            case 10: impPos = 2; break;
            case 100: impPos = 3; break;
        }
        editSpinnerImportance.setSelection(impPos);

        btnEditSelectDate.setOnClickListener(v -> showDatePicker());
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(selectedStartDate);
        new DatePickerDialog(this, (view, year, month, day) -> {
            c.set(year, month, day);
            new TimePickerDialog(this, (tv, hour, minute) -> {
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                selectedStartDate = c.getTimeInMillis();
                btnEditSelectDate.setText("Datum: " + day + "/" + (month+1) + " " + hour + ":" + String.format("%02d", minute));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String name = editTaskName.getText().toString().trim();
        String description = editTaskDescription.getText().toString().trim();

        if (name.isEmpty()) {
            editTaskName.setError("Naziv je obavezan!");
            return;
        }

        String[] difficulties = {"Veoma lak", "Lak", "Težak", "Ekstremno težak"};
        String[] importances = {"Normalan", "Važan", "Ekstremno važan", "Specijalan"};

        Task task = new Task();
        task.setFirestoreId(firestoreId);
        task.setName(name);
        task.setDescription(description);
        task.setDifficultyByLabel(difficulties[editSpinnerDifficulty.getSelectedItemPosition()]);
        task.setImportanceByLabel(importances[editSpinnerImportance.getSelectedItemPosition()]);
        task.setStartDate(selectedStartDate);
        task.setStatus("ACTIVE");
        task.setFrequencyType(taskFrequency != null && taskFrequency.equals("REPEATING") ?
                Task.FrequencyType.REPEATING : Task.FrequencyType.ONE_TIME);

        taskService.updateTask(task, new TaskService.OnTaskOperation() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(EditTaskActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(EditTaskActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
