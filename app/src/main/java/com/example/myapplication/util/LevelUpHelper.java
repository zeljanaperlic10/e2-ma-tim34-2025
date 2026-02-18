package com.example.myapplication.util;

import com.example.myapplication.data.model.User;

public class LevelUpHelper {

    // Provera da li korisnik treba da level up-uje
    public static boolean shouldLevelUp(User user) {
        return user.getXp() >= user.getRequiredXp();
    }

    // Izvršavanje level up-a
    public static void performLevelUp(User user) {
        int newLevel = user.getLevel() + 1;
        int remainingXp = user.getXp() - user.getRequiredXp();

        user.setLevel(newLevel);
        user.setXp(remainingXp);
        user.setRequiredXp(calculateRequiredXp(newLevel + 1));
        user.setPp(calculatePp(newLevel));
        user.setTitle(getTitleForLevel(newLevel));
    }

    // Formula za required XP: reqXP_nov = reqXP_stari * 2
    private static int calculateRequiredXp(int level) {
        if (level == 1) return 200;
        return calculateRequiredXp(level - 1) * 2;
    }

    // Formula za PP: PP_nov = PP_stari + 3/4 * PP_stari
    private static int calculatePp(int level) {
        if (level == 0) return 40;
        int prevPp = calculatePp(level - 1);
        return prevPp + (prevPp * 3 / 4);
    }

    // Titule po nivou
    private static String getTitleForLevel(int level) {
        if (level == 0) return "Početnik";
        if (level == 1) return "Ratnik";
        if (level == 2) return "Borac";
        if (level == 3) return "Veteran";
        if (level == 4) return "Šampion";
        if (level == 5) return "Majstor";
        if (level >= 6 && level <= 9) return "Legenda";
        return "Besmrtnik";
    }

    // Da li korisnik može da napreduje dalje (multiple level ups odjednom)
    public static void processAllLevelUps(User user) {
        while (shouldLevelUp(user)) {
            performLevelUp(user);
        }
    }
}