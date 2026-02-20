package com.example.myapplication.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.presentation.adapters.TaskAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerCalendarTasks;
    private TextView tvNoTasksForDay;

    private TaskAdapter taskAdapter;
    private TaskRepository taskRepository = new TaskRepository();
    private String userId;
    private List<Task> allTasks = new ArrayList<>();
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        calendarView = findViewById(R.id.calendarView);
        recyclerCalendarTasks = findViewById(R.id.recyclerCalendarTasks);
        tvNoTasksForDay = findViewById(R.id.tvNoTasksForDay);

        recyclerCalendarTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, new ArrayList<>(), task -> {
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra("FIRESTORE_ID", task.getFirestoreId());
            intent.putExtra("TASK_NAME", task.getName());
            intent.putExtra("TASK_DESCRIPTION", task.getDescription());
            intent.putExtra("TASK_CATEGORY", task.getCategory());
            intent.putExtra("TASK_CATEGORY_COLOR", task.getCategoryColor());
            intent.putExtra("TASK_DIFFICULTY", task.getDifficultyXP());
            intent.putExtra("TASK_IMPORTANCE", task.getImportanceXP());
            intent.putExtra("TASK_TOTAL_XP", task.getTotalXP());
            intent.putExtra("TASK_STATUS", task.getStatus());
            intent.putExtra("TASK_START_DATE", task.getStartDate());
            intent.putExtra("TASK_END_DATE", task.getEndDate() != null ? task.getEndDate() : 0L);
            intent.putExtra("TASK_FREQUENCY", task.getFrequencyType().name());
            startActivity(intent);
        });
        recyclerCalendarTasks.setAdapter(taskAdapter);

        selectedDate = System.currentTimeMillis();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDate = cal.getTimeInMillis();
            filterTasksForSelectedDay();
        });

        loadAllTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllTasks();
    }

    private void loadAllTasks() {
        taskRepository.getTasksForUser(userId, new TaskRepository.OnTasksLoaded() {
            @Override
            public void onSuccess(List<Task> tasks) {
                allTasks = tasks;
                filterTasksForSelectedDay();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CalendarActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTasksForSelectedDay() {
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDate);
        int selectedYear = selectedCal.get(Calendar.YEAR);
        int selectedMonth = selectedCal.get(Calendar.MONTH);
        int selectedDay = selectedCal.get(Calendar.DAY_OF_MONTH);

        List<Task> tasksForDay = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getStartDate() == null) continue;

            if (task.getFrequencyType() == Task.FrequencyType.ONE_TIME) {
                // Jednokratni: proveri samo startDate
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTimeInMillis(task.getStartDate());
                if (taskCal.get(Calendar.YEAR) == selectedYear &&
                        taskCal.get(Calendar.MONTH) == selectedMonth &&
                        taskCal.get(Calendar.DAY_OF_MONTH) == selectedDay) {
                    tasksForDay.add(task);
                }
            } else if (task.getFrequencyType() == Task.FrequencyType.REPEATING) {
                // Ponavljajući: generiši sve occurrence datume i provjeri
                if (taskOccursOnDate(task, selectedYear, selectedMonth, selectedDay)) {
                    tasksForDay.add(task);
                }
            }
        }

        taskAdapter.updateList(tasksForDay);

        if (tasksForDay.isEmpty()) {
            recyclerCalendarTasks.setVisibility(android.view.View.GONE);
            tvNoTasksForDay.setVisibility(android.view.View.VISIBLE);
        } else {
            recyclerCalendarTasks.setVisibility(android.view.View.VISIBLE);
            tvNoTasksForDay.setVisibility(android.view.View.GONE);
        }
    }

    /**
     * Proverava da li se ponavljajući zadatak pojavljuje na datom datumu.
     */
    private boolean taskOccursOnDate(Task task, int selYear, int selMonth, int selDay) {
        if (task.getRepeatInterval() == null || task.getRepeatUnit() == null) return false;

        Calendar taskStart = Calendar.getInstance();
        taskStart.setTimeInMillis(task.getStartDate());
        taskStart.set(Calendar.HOUR_OF_DAY, 0);
        taskStart.set(Calendar.MINUTE, 0);
        taskStart.set(Calendar.SECOND, 0);
        taskStart.set(Calendar.MILLISECOND, 0);

        Calendar selCal = Calendar.getInstance();
        selCal.set(selYear, selMonth, selDay, 0, 0, 0);
        selCal.set(Calendar.MILLISECOND, 0);

        // Selektovani datum mora biti >= start datuma
        if (selCal.before(taskStart)) return false;

        // Provjeri da li nije prošao end datum
        if (task.getEndDate() != null && task.getEndDate() > 0) {
            Calendar taskEnd = Calendar.getInstance();
            taskEnd.setTimeInMillis(task.getEndDate());
            taskEnd.set(Calendar.HOUR_OF_DAY, 23);
            if (selCal.after(taskEnd)) return false;
        }

        // Izračunaj razliku u danima
        long diffMs = selCal.getTimeInMillis() - taskStart.getTimeInMillis();
        long diffDays = diffMs / (24L * 60 * 60 * 1000);

        int interval = task.getRepeatInterval();
        if (task.getRepeatUnit() == Task.RepeatUnit.DAY) {
            return (diffDays % interval) == 0;
        } else if (task.getRepeatUnit() == Task.RepeatUnit.WEEK) {
            // Nedelja = 7 * interval dana
            return (diffDays % (interval * 7L)) == 0;
        }
        return false;
    }
}