package com.example.myapplication.presentation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.model.Category;
import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.domain.service.TaskService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {

    private EditText inputName, inputDescription, inputRepeatInterval;
    private Spinner spinnerCategory, spinnerRepeatUnit, spinnerDifficulty, spinnerImportance;
    private RadioGroup radioGroupFrequency;
    private LinearLayout layoutRepeatOptions;
    private Button btnSelectStartDate, btnSelectEndDate, btnAddTask;
    private TextView tvStartDate, tvEndDate;

    private long selectedStartDate = System.currentTimeMillis();
    private long selectedEndDate = 0L;
    private String userId;

    private TaskService taskService = new TaskService();
    private CategoryRepository categoryRepository = new CategoryRepository();
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Povezi view-ove
        inputName = findViewById(R.id.inputTaskName);
        inputDescription = findViewById(R.id.inputTaskDescription);
        inputRepeatInterval = findViewById(R.id.inputRepeatInterval);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerRepeatUnit = findViewById(R.id.spinnerRepeatUnit);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerImportance = findViewById(R.id.spinnerImportance);
        radioGroupFrequency = findViewById(R.id.radioGroupFrequency);
        layoutRepeatOptions = findViewById(R.id.layoutRepeatOptions);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnAddTask = findViewById(R.id.btnAddTask);

        // Spinneri za težinu i bitnost
        String[] difficulties = {"Veoma lak", "Lak", "Težak", "Ekstremno težak"};
        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, difficulties));

        String[] importances = {"Normalan", "Važan", "Ekstremno važan", "Specijalan"};
        spinnerImportance.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, importances));

        String[] repeatUnits = {"Dan", "Nedelja"};
        spinnerRepeatUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, repeatUnits));

        // Učitaj kategorije iz Firebase
        loadCategories();

        // Prikaz opcija za ponavljanje
        radioGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioRepeating) {
                layoutRepeatOptions.setVisibility(LinearLayout.VISIBLE);
                btnSelectEndDate.setVisibility(Button.VISIBLE);
            } else {
                layoutRepeatOptions.setVisibility(LinearLayout.GONE);
                btnSelectEndDate.setVisibility(Button.GONE);
            }
        });

        btnSelectStartDate.setOnClickListener(v -> showDatePicker(true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(false));
        btnAddTask.setOnClickListener(v -> addTask());
    }

    private void loadCategories() {
        categoryRepository.getCategories(userId, new CategoryRepository.OnCategoriesLoaded() {
            @Override
            public void onSuccess(List<Category> categories) {
                categoryList = categories;
                List<String> names = new ArrayList<>();
                for (Category c : categories) {
                    names.add(c.getName());
                }
                if (names.isEmpty()) {
                    names.add("Nema kategorija — dodaj ih u Kategorije");
                }
                spinnerCategory.setAdapter(new ArrayAdapter<>(
                        AddTaskActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        names));
            }
            @Override
            public void onError(String message) {
                Toast.makeText(AddTaskActivity.this,
                        "Greška pri učitavanju kategorija: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(boolean isStart) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            c.set(year, month, day);
            new TimePickerDialog(this, (tv, hour, minute) -> {
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                if (isStart) {
                    selectedStartDate = c.getTimeInMillis();
                    btnSelectStartDate.setText("Početak: " + day + "/" + (month+1) + " " + hour + ":" + String.format("%02d", minute));
                } else {
                    selectedEndDate = c.getTimeInMillis();
                    btnSelectEndDate.setText("Kraj: " + day + "/" + (month+1) + " " + hour + ":" + String.format("%02d", minute));
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addTask() {
        String name = inputName.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();

        if (name.isEmpty()) {
            inputName.setError("Naziv je obavezan!");
            return;
        }

        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Prvo dodaj kategoriju!", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = categoryList.get(spinnerCategory.getSelectedItemPosition());

        boolean isRepeating = radioGroupFrequency.getCheckedRadioButtonId() == R.id.radioRepeating;
        Task.FrequencyType frequency = isRepeating ?
                Task.FrequencyType.REPEATING : Task.FrequencyType.ONE_TIME;

        Integer interval = null;
        Task.RepeatUnit repeatUnit = null;

        if (isRepeating) {
            String intervalStr = inputRepeatInterval.getText().toString().trim();
            if (intervalStr.isEmpty()) {
                inputRepeatInterval.setError("Unesi interval!");
                return;
            }
            interval = Integer.parseInt(intervalStr);
            if (interval <= 0) {
                inputRepeatInterval.setError("Interval mora biti veći od 0!");
                return;
            }
            repeatUnit = spinnerRepeatUnit.getSelectedItemPosition() == 0 ?
                    Task.RepeatUnit.DAY : Task.RepeatUnit.WEEK;

            if (selectedEndDate <= selectedStartDate) {
                Toast.makeText(this, "Datum završetka mora biti nakon početka!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String[] difficulties = {"Veoma lak", "Lak", "Težak", "Ekstremno težak"};
        String[] importances = {"Normalan", "Važan", "Ekstremno važan", "Specijalan"};

        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setCategory(selectedCategory.getName());
        task.setCategoryColor(selectedCategory.getColor());
        task.setFrequencyType(frequency);
        task.setRepeatInterval(interval);
        task.setRepeatUnit(repeatUnit);
        task.setStartDate(selectedStartDate);
        task.setEndDate(isRepeating ? selectedEndDate : null);
        task.setDifficultyByLabel(difficulties[spinnerDifficulty.getSelectedItemPosition()]);
        task.setImportanceByLabel(importances[spinnerImportance.getSelectedItemPosition()]);
        task.setUserId(userId);
        task.setCreatedTimestamp(System.currentTimeMillis());
        task.setStatus("ACTIVE");

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



