package com.example.myapplication.domain.service;

import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.repository.TaskRepository;

import java.util.Calendar;

public class TaskService {
    private final TaskRepository taskRepository = new TaskRepository();

    public void addTask(Task task, OnTaskOperation callback) {
        // Osnovna validacija
        if (task.getName() == null || task.getName().isEmpty()) {
            callback.onError("Naziv zadatka je obavezan!");
            return;
        }
        if (task.getCategory() == null || task.getCategory().isEmpty()) {
            callback.onError("Odaberi kategoriju!");
            return;
        }
        if (task.getUserId() == null || task.getUserId().isEmpty()) {
            callback.onError("Neispravan korisnik!");
            return;
        }

        if (task.getFrequencyType() == Task.FrequencyType.REPEATING) {
            if (task.getRepeatInterval() == null || task.getRepeatInterval() <= 0) {
                callback.onError("Interval ponavljanja mora biti veći od 0!");
                return;
            }
            if (task.getRepeatUnit() == null) {
                callback.onError("Odaberi jedinicu ponavljanja (Dan/Nedelja)!");
                return;
            }
            if (task.getEndDate() != null && task.getEndDate() <= task.getStartDate()) {
                callback.onError("Datum završetka mora biti nakon početka!");
                return;
            }
        }

        // Proveravamo kvote za difficulty i importance asinhrono
        Calendar now = Calendar.getInstance();
        long startOfDay = getStartOfDay(now);
        long endOfDay = getEndOfDay(now);

        // Prvo proverimo difficulty kvotu
        taskRepository.getCompletedTaskCountForPeriod(
                task.getUserId(),
                task.getDifficultyXP(),
                -1, // -1 znači da ne filtrira po importance
                startOfDay,
                endOfDay,
                new TaskRepository.OnTaskCount() {
                    @Override
                    public void onSuccess(int difficultyCount) {
                        boolean canGrantDifficulty = canGrantDifficulty(task.getDifficultyXP(), difficultyCount);
                        int grantedDifficultyXP = canGrantDifficulty ? task.getDifficultyXP() : 0;

                        // Sada proveravamo importance kvotu
                        taskRepository.getCompletedTaskCountForPeriod(
                                task.getUserId(),
                                -1, // -1 znači da ne filtrira po difficulty
                                task.getImportanceXP(),
                                startOfDay,
                                endOfDay,
                                new TaskRepository.OnTaskCount() {
                                    @Override
                                    public void onSuccess(int importanceCount) {
                                        boolean canGrantImportance = canGrantImportance(task.getImportanceXP(), importanceCount);
                                        int grantedImportanceXP = canGrantImportance ? task.getImportanceXP() : 0;

                                        // Postavljamo XP i ukupno
                                        task.setDifficultyXP(grantedDifficultyXP);
                                        task.setImportanceXP(grantedImportanceXP);
                                        task.setTotalXP(grantedDifficultyXP + grantedImportanceXP);

                                        // Dodajemo zadatak u bazu
                                        taskRepository.addTask(task, new TaskRepository.OnTaskAdded() {
                                            @Override
                                            public void onSuccess() {
                                                callback.onSuccess("Zadatak uspešno dodat!");
                                            }
                                            @Override
                                            public void onError(String message) {
                                                callback.onError("Greška pri dodavanju zadatka: " + message);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError("Greška pri proveri importance kvote: " + message);
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("Greška pri proveri difficulty kvote: " + message);
                    }
                });
    }

    // Helper metode za kvotu
    private boolean canGrantDifficulty(int difficultyXP, int count) {
        switch (difficultyXP) {
            case 1: return count < 5;   // Veoma lak
            case 3: return count < 5;   // Lak
            case 7: return count < 2;   // Težak
            case 20: return count < 1;  // Ekstremno težak
        }
        return true;
    }

    private boolean canGrantImportance(int importanceXP, int count) {
        switch (importanceXP) {
            case 1: return count < 5;    // Normalan
            case 3: return count < 5;    // Važan
            case 10: return count < 2;   // Ekstremno važan
            case 100: return count < 1;  // Specijalan
        }
        return true;
    }

    // Vreme početka i kraja dana
    private long getStartOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long getEndOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    // Callback interfejs
    public interface OnTaskOperation {
        void onSuccess(String message);
        void onError(String message);
    }
}





