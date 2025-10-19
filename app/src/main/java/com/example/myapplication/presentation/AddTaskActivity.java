package com.example.myapplication.presentation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.model.Task;
import com.example.myapplication.domain.service.TaskService;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private EditText inputTaskName, inputTaskDescription, inputRepeatInterval;
    private Spinner spinnerCategory, spinnerRepeatUnit, spinnerDifficulty, spinnerImportance;
    private RadioGroup radioGroupFrequency;
    private LinearLayout layoutRepeatOptions;
    private Button btnCategoryColor, btnSelectStartDate, btnSelectEndDate, btnAddTask;

    private int selectedCategoryColor = Color.BLUE;
    private long selectedStartDate = System.currentTimeMillis();
    private long selectedEndDate = 0L;

    private TaskService taskService = new TaskService();
    private Map<String, Integer> categoryColors;

    // UID korisnika koji kreira zadatak
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Preuzimanje UID-a iz intent-a
        userId = getIntent().getStringExtra("USER_ID");

        // Poveži view-ove
        inputTaskName = findViewById(R.id.inputTaskName);
        inputTaskDescription = findViewById(R.id.inputTaskDescription);
        inputRepeatInterval = findViewById(R.id.inputRepeatInterval);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerRepeatUnit = findViewById(R.id.spinnerRepeatUnit);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerImportance = findViewById(R.id.spinnerImportance);
        radioGroupFrequency = findViewById(R.id.radioGroupFrequency);
        layoutRepeatOptions = findViewById(R.id.layoutRepeatOptions);
        btnCategoryColor = findViewById(R.id.btnCategoryColor);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnAddTask = findViewById(R.id.btnAddTask);

        // Kategorije i boje
        categoryColors = new HashMap<>();
        categoryColors.put("Zdravlje", Color.GREEN);
        categoryColors.put("Učenje", Color.BLUE);
        categoryColors.put("Zabava", Color.MAGENTA);
        categoryColors.put("Sređivanje", Color.YELLOW);

        // Spinner za kategorije
        String[] categories = {"Zdravlje", "Učenje", "Zabava", "Sređivanje"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        // Spinneri za interval, težinu i bitnost
        String[] repeatUnits = {"Dan", "Nedelja"};
        spinnerRepeatUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, repeatUnits));

        String[] difficulties = {"Veoma lak", "Lak", "Težak", "Ekstremno težak"};
        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, difficulties));

        String[] importances = {"Normalan", "Važan", "Ekstremno važan", "Specijalan"};
        spinnerImportance.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, importances));

        // Prikaz opcija za ponavljanje samo ako je ponavljajući
        radioGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioRepeating) {
                layoutRepeatOptions.setVisibility(LinearLayout.VISIBLE);
                btnSelectEndDate.setVisibility(Button.VISIBLE);
            } else {
                layoutRepeatOptions.setVisibility(LinearLayout.GONE);
                btnSelectEndDate.setVisibility(Button.GONE);
            }
        });

        // Dugmad za datum
        btnSelectStartDate.setOnClickListener(v -> showDateTimePicker(true));
        btnSelectEndDate.setOnClickListener(v -> showDateTimePicker(false));

        // Dugme za dodavanje zadatka
        btnAddTask.setOnClickListener(v -> addTask());
    }

    private void showDateTimePicker(boolean isStartDate) {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(AddTaskActivity.this, (timeView, hourOfDay, minute) -> {
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                if (isStartDate) {
                    selectedStartDate = c.getTimeInMillis();
                    btnSelectStartDate.setText("Datum početka: " + dayOfMonth + "/" + (month + 1) + " " + hourOfDay + ":" + minute);
                } else {
                    selectedEndDate = c.getTimeInMillis();
                    btnSelectEndDate.setText("Datum završetka: " + dayOfMonth + "/" + (month + 1) + " " + hourOfDay + ":" + minute);
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();

        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addTask() {
        String name = inputTaskName.getText().toString();
        String description = inputTaskDescription.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        selectedCategoryColor = categoryColors.get(category);

        Task.FrequencyType frequency = radioGroupFrequency.getCheckedRadioButtonId() == R.id.radioRepeating ?
                Task.FrequencyType.REPEATING : Task.FrequencyType.ONE_TIME;

        Integer interval = null;
        Task.RepeatUnit repeatUnit = null;

        if (frequency == Task.FrequencyType.REPEATING) {
            String intervalStr = inputRepeatInterval.getText().toString();
            interval = intervalStr.isEmpty() ? 1 : Integer.parseInt(intervalStr);
            if (interval <= 0) {
                Toast.makeText(this, "Interval ponavljanja mora biti veći od 0", Toast.LENGTH_SHORT).show();
                return;
            }
            repeatUnit = spinnerRepeatUnit.getSelectedItemPosition() == 0 ? Task.RepeatUnit.DAY : Task.RepeatUnit.WEEK;

            if (selectedEndDate <= selectedStartDate) {
                Toast.makeText(this, "Datum završetka mora biti nakon početka", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int difficultyXP = spinnerDifficulty.getSelectedItemPosition() == 0 ? 1 :
                spinnerDifficulty.getSelectedItemPosition() == 1 ? 3 :
                        spinnerDifficulty.getSelectedItemPosition() == 2 ? 7 : 20;

        int importanceXP = spinnerImportance.getSelectedItemPosition() == 0 ? 1 :
                spinnerImportance.getSelectedItemPosition() == 1 ? 3 :
                        spinnerImportance.getSelectedItemPosition() == 2 ? 10 : 100;

        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setCategory(category);
        task.setCategoryColor(selectedCategoryColor);
        task.setFrequencyType(frequency);
        task.setRepeatInterval(interval);
        task.setRepeatUnit(repeatUnit);
        task.setStartDate(selectedStartDate);
        task.setEndDate(frequency == Task.FrequencyType.REPEATING ? selectedEndDate : null);
        task.setDifficultyXP(difficultyXP);
        task.setImportanceXP(importanceXP);

        // Setovanje UID korisnika
        task.setUserId(userId);

        // Setovanje timestamp-a kreiranja
        task.setCreatedTimestamp(System.currentTimeMillis());

        taskService.addTask(task, new TaskService.OnTaskOperation() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(AddTaskActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AddTaskActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}




