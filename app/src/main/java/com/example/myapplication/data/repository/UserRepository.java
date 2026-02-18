package com.example.myapplication.data.repository;

import com.example.myapplication.data.model.User;
import com.example.myapplication.util.LevelUpHelper;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getUser(String userId, OnUserLoaded callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);

                        // Dodaj default vrednosti ako ne postoje
                        if (user.getPp() == 0) {
                            user.setLevel(0);
                            user.setXp(0);
                            user.setPp(40);
                            user.setRequiredXp(200);
                            user.setCoins(0);
                            user.setTitle("Početnik");
                            user.setLastLevelUpTimestamp(System.currentTimeMillis());

                            // Sačuvaj u bazu
                            updateUser(userId, user, new OnOperationComplete() {
                                @Override
                                public void onSuccess() {
                                    callback.onSuccess(user);
                                }
                                @Override
                                public void onError(String message) {
                                    callback.onSuccess(user); // Vrati user čak i ako update failu
                                }
                            });
                        } else {
                            callback.onSuccess(user);
                        }
                    } else {
                        callback.onError("Korisnik ne postoji");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateUser(String userId, User user, OnOperationComplete callback) {
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(a -> {
                    android.util.Log.d("USER_REPO", "User updated: level=" + user.getLevel() + ", xp=" + user.getXp());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("USER_REPO", "Update failed: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    public void addXp(String userId, int xpToAdd, OnXpAdded callback) {
        android.util.Log.d("USER_REPO", "Adding XP: " + xpToAdd + " to user: " + userId);

        getUser(userId, new OnUserLoaded() {
            @Override
            public void onSuccess(User user) {
                android.util.Log.d("USER_REPO", "User loaded: level=" + user.getLevel() + ", xp=" + user.getXp());

                int oldLevel = user.getLevel();
                int oldPp = user.getPp();
                user.setXp(user.getXp() + xpToAdd);

                android.util.Log.d("USER_REPO", "New XP: " + user.getXp() + " / " + user.getRequiredXp());

                boolean leveledUp = false;
                if (LevelUpHelper.shouldLevelUp(user)) {
                    LevelUpHelper.processAllLevelUps(user);
                    user.setLastLevelUpTimestamp(System.currentTimeMillis());
                    leveledUp = true;
                    android.util.Log.d("USER_REPO", "LEVEL UP!");
                }

                boolean finalLeveledUp = leveledUp;
                int newLevel = user.getLevel();
                int newPp = user.getPp();

                updateUser(userId, user, new OnOperationComplete() {
                    @Override
                    public void onSuccess() {
                        android.util.Log.d("USER_REPO", "User saved successfully!");
                        if (finalLeveledUp) {
                            callback.onLevelUp(oldLevel, newLevel, oldPp, newPp, user.getTitle(), user.getRequiredXp());
                        } else {
                            callback.onXpAdded();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        android.util.Log.e("USER_REPO", "Save failed: " + message);
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("USER_REPO", "Failed to load user: " + message);
                callback.onError(message);
            }
        });
    }

    public void addCoins(String userId, int coinsToAdd, OnOperationComplete callback) {
        getUser(userId, new OnUserLoaded() {
            @Override
            public void onSuccess(User user) {
                user.setCoins(user.getCoins() + coinsToAdd);
                updateUser(userId, user, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public interface OnUserLoaded {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface OnOperationComplete {
        void onSuccess();
        void onError(String message);
    }

    public interface OnXpAdded {
        void onXpAdded();
        void onLevelUp(int oldLevel, int newLevel, int oldPp, int newPp, String newTitle, int newRequiredXp);
        void onError(String message);
    }
}