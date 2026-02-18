package com.example.myapplication.domain.service;

import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskService {

    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();

    // Dodavanje zadatka sa kvota proverom
    public void addTask(Task task, OnTaskOperation callback) {

        // Validacija
        if (task.getName() == null || task.getName().trim().isEmpty()) {
            callback.onError("Naziv zadatka je obavezan!");
            return;
        }
        if (task.getCategory() == null || task.getCategory().trim().isEmpty()) {
            callback.onError("Odaberi kategoriju!");
            return;
        }
        if (task.getUserId() == null || task.getUserId().trim().isEmpty()) {
            callback.onError("Neispravan korisnik!");
            return;
        }
        if (task.getFrequencyType() == Task.FrequencyType.REPEATING) {
            if (task.getRepeatInterval() == null || task.getRepeatInterval() <= 0) {
                callback.onError("Interval ponavljanja mora biti veći od 0!");
                return;
            }
            if (task.getRepeatUnit() == null) {
                callback.onError("Odaberi jedinicu ponavljanja!");
                return;
            }
            if (task.getEndDate() != null && task.getEndDate() <= task.getStartDate()) {
                callback.onError("Datum završetka mora biti nakon početka!");
                return;
            }
        }

        Calendar now = Calendar.getInstance();
        long startOfDay = getStartOfDay(now);
        long endOfDay = getEndOfDay(now);

        // Provera kvote za difficulty
        taskRepository.getCompletedTaskCountForPeriod(
                task.getUserId(),
                task.getDifficultyXP(),
                startOfDay, endOfDay,
                new TaskRepository.OnTaskCount() {
                    @Override
                    public void onSuccess(int diffCount) {
                        boolean canDiff = canGrantDifficulty(task.getDifficultyXP(), diffCount);

                        // Provera kvote za importance
                        taskRepository.getCompletedImportanceCountForPeriod(
                                task.getUserId(),
                                task.getImportanceXP(),
                                startOfDay, endOfDay,
                                new TaskRepository.OnTaskCount() {
                                    @Override
                                    public void onSuccess(int impCount) {
                                        boolean canImp = canGrantImportance(task.getImportanceXP(), impCount);

                                        task.setDifficultyXP(canDiff ? task.getDifficultyXP() : 0);
                                        task.setImportanceXP(canImp ? task.getImportanceXP() : 0);
                                        task.setTotalXP(task.getDifficultyXP() + task.getImportanceXP());

                                        taskRepository.addTask(task, new TaskRepository.OnOperationComplete() {
                                            @Override
                                            public void onSuccess() {
                                                callback.onSuccess("Zadatak uspešno dodat!");
                                            }
                                            @Override
                                            public void onError(String message) {
                                                callback.onError(message);
                                            }
                                        });
                                    }
                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                });
                    }
                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    // Označavanje zadatka kao urađen
    public void markTaskDone(Task task, OnTaskOperation callback) {
        if (!"ACTIVE".equals(task.getStatus())) {
            callback.onError("Samo aktivan zadatak može biti označen kao urađen!");
            return;
        }
        if (!task.canBeMarked()) {
            callback.onError("Zadatak ne može biti označen — prošlo je više od 3 dana!");
            return;
        }

        taskRepository.updateTaskStatus(
                task.getFirestoreId(), "DONE",
                System.currentTimeMillis(),
                new TaskRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() {
                        // Dodaj XP korisniku
                        userRepository.addXp(task.getUserId(), task.getTotalXP(), new UserRepository.OnXpAdded() {
                            @Override
                            public void onXpAdded() {
                                callback.onSuccess("Zadatak označen kao urađen! +" + task.getTotalXP() + " XP");
                            }

                            @Override
                            public void onLevelUp(int oldLevel, int newLevel, int oldPp, int newPp, String newTitle, int newRequiredXp) {
                                callback.onLevelUp(oldLevel, newLevel, oldPp, newPp, newTitle, newRequiredXp);
                            }

                            @Override
                            public void onError(String message) {
                                callback.onSuccess("Zadatak urađen, ali greška sa XP: " + message);
                            }
                        });
                    }
                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    // Označavanje zadatka kao otkazan
    public void markTaskCancelled(Task task, OnTaskOperation callback) {
        if (!"ACTIVE".equals(task.getStatus())) {
            callback.onError("Samo aktivan zadatak može biti otkazan!");
            return;
        }
        taskRepository.updateTaskStatus(
                task.getFirestoreId(), "CANCELLED", null,
                new TaskRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() { callback.onSuccess("Zadatak otkazan!"); }
                    @Override
                    public void onError(String message) { callback.onError(message); }
                });
    }

    // Pauziranje zadatka (samo ponavljajući)
    public void pauseTask(Task task, OnTaskOperation callback) {
        if (!"ACTIVE".equals(task.getStatus())) {
            callback.onError("Samo aktivan zadatak može biti pauziran!");
            return;
        }
        if (task.getFrequencyType() != Task.FrequencyType.REPEATING) {
            callback.onError("Samo ponavljajući zadatak može biti pauziran!");
            return;
        }
        taskRepository.updateTaskStatus(
                task.getFirestoreId(), "PAUSED", null,
                new TaskRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() { callback.onSuccess("Zadatak pauziran!"); }
                    @Override
                    public void onError(String message) { callback.onError(message); }
                });
    }

    // Aktiviranje pauziranog zadatka
    public void activateTask(Task task, OnTaskOperation callback) {
        if (!"PAUSED".equals(task.getStatus())) {
            callback.onError("Samo pauziran zadatak može biti aktiviran!");
            return;
        }
        taskRepository.updateTaskStatus(
                task.getFirestoreId(), "ACTIVE", null,
                new TaskRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() { callback.onSuccess("Zadatak aktiviran!"); }
                    @Override
                    public void onError(String message) { callback.onError(message); }
                });
    }

    // Brisanje zadatka
    public void deleteTask(Task task, OnTaskOperation callback) {
        if ("DONE".equals(task.getStatus())) {
            callback.onError("Završeni zadaci ne mogu biti obrisani!");
            return;
        }
        if (task.getFrequencyType() == Task.FrequencyType.REPEATING) {
            taskRepository.deleteFutureRepeatingTasks(
                    task.getUserId(), task.getName(),
                    System.currentTimeMillis(),
                    new TaskRepository.OnOperationComplete() {
                        @Override
                        public void onSuccess() { callback.onSuccess("Budući zadaci obrisani!"); }
                        @Override
                        public void onError(String message) { callback.onError(message); }
                    });
        } else {
            taskRepository.deleteTask(task.getFirestoreId(),
                    new TaskRepository.OnOperationComplete() {
                        @Override
                        public void onSuccess() { callback.onSuccess("Zadatak obrisan!"); }
                        @Override
                        public void onError(String message) { callback.onError(message); }
                    });
        }
    }

    // Izmena zadatka
    public void updateTask(Task task, OnTaskOperation callback) {
        if (!task.canBeEdited()) {
            callback.onError("Ovaj zadatak ne može biti izmenjen!");
            return;
        }
        taskRepository.updateTask(task,
                new TaskRepository.OnOperationComplete() {
                    @Override
                    public void onSuccess() { callback.onSuccess("Zadatak izmenjen!"); }
                    @Override
                    public void onError(String message) { callback.onError(message); }
                });
    }

    // Učitavanje svih zadataka korisnika
    public void getTasksForUser(String userId, TaskRepository.OnTasksLoaded callback) {
        taskRepository.getTasksForUser(userId, callback);
    }

    // Helper metode za kvotu
    private boolean canGrantDifficulty(int difficultyXP, int count) {
        switch (difficultyXP) {
            case 1:  return count < 5;  // Veoma lak
            case 3:  return count < 5;  // Lak
            case 7:  return count < 2;  // Težak
            case 20: return count < 1;  // Ekstremno težak
        }
        return true;
    }

    private boolean canGrantImportance(int importanceXP, int count) {
        switch (importanceXP) {
            case 1:   return count < 5;  // Normalan
            case 3:   return count < 5;  // Važan
            case 10:  return count < 2;  // Ekstremno važan
            case 100: return count < 1;  // Specijalan
        }
        return true;
    }

    private long getStartOfDay(Calendar c) {
        Calendar cal = (Calendar) c.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDay(Calendar c) {
        Calendar cal = (Calendar) c.clone();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public interface OnTaskOperation {
        void onSuccess(String message);
        void onError(String message);
        default void onLevelUp(int oldLevel, int newLevel, int oldPp, int newPp, String newTitle, int newRequiredXp) {}
    }
}





