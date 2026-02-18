package com.example.myapplication.util;

import com.example.myapplication.data.model.Task;

import java.util.List;

public class TaskSuccessCalculator {

    // Računanje procenta uspešnosti zadataka u etapi (između dva nivoa)
    public static int calculateSuccessPercent(List<Task> allTasks) {
        if (allTasks == null || allTasks.isEmpty()) return 0;

        int totalTasks = 0;
        int completedTasks = 0;

        for (Task task : allTasks) {
            String status = task.getStatus();

            // Preskačemo pauzirane i otkazane (ne računaju se u uspešnost)
            if ("PAUSED".equals(status) || "CANCELLED".equals(status)) {
                continue;
            }

            totalTasks++;

            if ("DONE".equals(status)) {
                completedTasks++;
            }
        }

        if (totalTasks == 0) return 0;

        return (int) ((float) completedTasks / totalTasks * 100);
    }

    // Računanje procenta uspešnosti samo u određenom vremenskom periodu (etapi)
    public static int calculateSuccessPercentForPeriod(List<Task> allTasks, long periodStart, long periodEnd) {
        if (allTasks == null || allTasks.isEmpty()) return 0;

        int totalTasks = 0;
        int completedTasks = 0;

        for (Task task : allTasks) {
            if (task.getCreatedTimestamp() == null) continue;

            // Samo zadaci kreirani u etapi
            if (task.getCreatedTimestamp() < periodStart || task.getCreatedTimestamp() > periodEnd) {
                continue;
            }

            String status = task.getStatus();

            // Preskačemo pauzirane i otkazane
            if ("PAUSED".equals(status) || "CANCELLED".equals(status)) {
                continue;
            }

            // Preskačemo zadatke koji nemaju XP (prešli su kvotu)
            if (task.getTotalXP() == 0) {
                continue;
            }

            totalTasks++;

            if ("DONE".equals(status)) {
                completedTasks++;
            }
        }

        if (totalTasks == 0) return 0;

        return (int) ((float) completedTasks / totalTasks * 100);
    }
}