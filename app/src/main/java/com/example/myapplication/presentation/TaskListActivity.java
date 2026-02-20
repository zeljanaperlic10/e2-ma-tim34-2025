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
import com.example.myapplication.data.model.Task;
import com.example.myapplication.domain.service.TaskService;
import com.example.myapplication.presentation.adapters.TaskAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerTasks;
    private TextView tvEmpty;
    private Button btnAddTask;
    private TabLayout tabLayout;

    private TaskAdapter taskAdapter;
    private TaskService taskService = new TaskService();
    private String userId;

    // Čuvamo sve zadatke da možemo filtrirati po tabu
    private List<Task> allTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Povezi view-ove
        recyclerTasks = findViewById(R.id.recyclerTasks);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddTask = findViewById(R.id.btnAddTask);
        tabLayout = findViewById(R.id.tabLayout);

        // Postavi RecyclerView
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, new ArrayList<>(), task -> {
            // Klik na zadatak — otvori detalje
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
        recyclerTasks.setAdapter(taskAdapter);

        // Tabovi
        tabLayout.addTab(tabLayout.newTab().setText("Jednokratni"));
        tabLayout.addTab(tabLayout.newTab().setText("Ponavljajući"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterTasks(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Dugme za dodavanje zadatka
        btnAddTask.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTaskActivity.class));
        });

        // Učitaj zadatke
        loadTasks();
    }

    // Vraćamo se sa AddTaskActivity ili EditTaskActivity — osvežavamo listu
    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        taskService.getTasksForUser(userId, new com.example.myapplication.data.repository.TaskRepository.OnTasksLoaded() {
            @Override
            public void onSuccess(List<Task> tasks) {
                // Prikazuj samo trenutne i buduće zadatke (ne prošle)
                allTasks.clear();
                // Početak današnjeg dana (00:00:00)
                java.util.Calendar calToday = java.util.Calendar.getInstance();
                calToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calToday.set(java.util.Calendar.MINUTE, 0);
                calToday.set(java.util.Calendar.SECOND, 0);
                calToday.set(java.util.Calendar.MILLISECOND, 0);
                long startOfToday = calToday.getTimeInMillis();

                for (Task t : tasks) {
                    // Neurađeni i otkazani se ne prikazuju u listi
                    if ("UNDONE".equals(t.getStatus()) || "CANCELLED".equals(t.getStatus())) continue;
                    // Završeni čiji je datum PRE danas — samo u kalendaru
                    if ("DONE".equals(t.getStatus()) && t.getStartDate() != null && t.getStartDate() < startOfToday) continue;
                    // Jednokratni aktivni čiji je datum PRE danas — samo u kalendaru
                    if ("ACTIVE".equals(t.getStatus())
                            && t.getFrequencyType() == Task.FrequencyType.ONE_TIME
                            && t.getStartDate() != null && t.getStartDate() < startOfToday) continue;
                    allTasks.add(t);
                }
                // Prikaži tab koji je trenutno selektovan
                int selectedTab = tabLayout.getSelectedTabPosition();
                filterTasks(selectedTab);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(TaskListActivity.this,
                        "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTasks(int tabPosition) {
        List<Task> filtered = new ArrayList<>();
        for (Task t : allTasks) {
            if (tabPosition == 0 && t.getFrequencyType() == Task.FrequencyType.ONE_TIME) {
                filtered.add(t);
            } else if (tabPosition == 1 && t.getFrequencyType() == Task.FrequencyType.REPEATING) {
                filtered.add(t);
            }
        }

        taskAdapter.updateList(filtered);

        // Prikaži poruku ako nema zadataka
        if (filtered.isEmpty()) {
            recyclerTasks.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerTasks.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}