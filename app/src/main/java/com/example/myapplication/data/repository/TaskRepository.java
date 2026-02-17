package com.example.myapplication.data.repository;

import com.example.myapplication.data.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addTask(Task task, OnOperationComplete callback) {
        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(ref -> {
                    task.setFirestoreId(ref.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getTasksForUser(String userId, OnTasksLoaded callback) {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Task> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Task t = doc.toObject(Task.class);
                        t.setFirestoreId(doc.getId());
                        list.add(t);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateTaskStatus(String firestoreId, String status, Long completedDate, OnOperationComplete callback) {
        db.collection("tasks").document(firestoreId)
                .update("status", status, "completedDate", completedDate)
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateTask(Task task, OnOperationComplete callback) {
        db.collection("tasks").document(task.getFirestoreId())
                .update(
                        "name", task.getName(),
                        "description", task.getDescription(),
                        "startDate", task.getStartDate(),
                        "difficultyXP", task.getDifficultyXP(),
                        "importanceXP", task.getImportanceXP(),
                        "totalXP", task.getTotalXP()
                )
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteTask(String firestoreId, OnOperationComplete callback) {
        db.collection("tasks").document(firestoreId)
                .delete()
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteFutureRepeatingTasks(String userId, String taskName, long fromDate, OnOperationComplete callback) {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("name", taskName)
                .whereEqualTo("frequencyType", "REPEATING")
                .whereGreaterThanOrEqualTo("startDate", fromDate)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getCompletedTaskCountForPeriod(String userId, int difficultyXP,
                                               long periodStart, long periodEnd,
                                               OnTaskCount callback) {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "DONE")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Task t = doc.toObject(Task.class);
                        if (t.getDifficultyXP() == difficultyXP &&
                                t.getCompletedDate() != null &&
                                t.getCompletedDate() >= periodStart &&
                                t.getCompletedDate() <= periodEnd) {
                            count++;
                        }
                    }
                    callback.onSuccess(count);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getCompletedImportanceCountForPeriod(String userId, int importanceXP,
                                                     long periodStart, long periodEnd,
                                                     OnTaskCount callback) {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "DONE")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Task t = doc.toObject(Task.class);
                        if (t.getImportanceXP() == importanceXP &&
                                t.getCompletedDate() != null &&
                                t.getCompletedDate() >= periodStart &&
                                t.getCompletedDate() <= periodEnd) {
                            count++;
                        }
                    }
                    callback.onSuccess(count);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface OnOperationComplete {
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

