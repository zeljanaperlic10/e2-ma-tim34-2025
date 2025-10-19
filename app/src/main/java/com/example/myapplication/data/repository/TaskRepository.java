package com.example.myapplication.data.repository;

import androidx.annotation.NonNull;

import com.example.myapplication.data.model.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference tasksCollection = db.collection("tasks");

    // Dodavanje zadatka
    public void addTask(Task task, OnTaskAdded callback) {
        tasksCollection.add(task)
                .addOnSuccessListener(documentReference -> {
                    task.setId(documentReference.getId().hashCode());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Čitanje svih zadataka (za admin/test)
    public void getAllTasks(OnTasksLoaded callback) {
        tasksCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task> taskList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Task t = doc.toObject(Task.class);
                    if (t != null) taskList.add(t);
                }
                callback.onSuccess(taskList);
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    // Čitanje zadataka samo za konkretnog korisnika
    public void getTasksForUser(String userId, OnTasksLoaded callback) {
        tasksCollection.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> taskList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Task t = doc.toObject(Task.class);
                            if (t != null) taskList.add(t);
                        }
                        callback.onSuccess(taskList);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // Broj izvršenih zadataka korisnika za određeni period, težinu i bitnost
    public void getCompletedTaskCountForPeriod(String userId, int difficultyXP, int importanceXP,
                                               long periodStart, long periodEnd,
                                               OnTaskCount callback) {
        tasksCollection.whereEqualTo("userId", userId)
                .whereEqualTo("difficultyXP", difficultyXP)
                .whereEqualTo("importanceXP", importanceXP)
                .whereGreaterThanOrEqualTo("completedDate", periodStart)
                .whereLessThanOrEqualTo("completedDate", periodEnd)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        callback.onSuccess(count);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // Callback interfejsi
    public interface OnTaskAdded {
        void onSuccess();
        void onError(String message);
    }

    public interface OnTasksLoaded {
        void onSuccess(List<Task> tasks);
        void onError(String message);
    }

    public interface OnTaskCount {
        void onSuccess(int count);
        void onError(String message);
    }
}


