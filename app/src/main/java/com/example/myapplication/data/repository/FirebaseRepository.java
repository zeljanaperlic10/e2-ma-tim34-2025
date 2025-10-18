package com.example.myapplication.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void registerUser(String email, String password, String username, String avatar, OnUserRegistered callback) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Username već postoji
                            callback.onError("Korisničko ime je već zauzeto!");
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            FirebaseUser firebaseUser = auth.getCurrentUser();

                                            if (firebaseUser != null) {
                                                firebaseUser.sendEmailVerification();

                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("id", firebaseUser.getUid());
                                                userMap.put("username", username);
                                                userMap.put("email", email);
                                                userMap.put("avatar", avatar);
                                                userMap.put("activated", false);
                                                userMap.put("registrationTime", System.currentTimeMillis());

                                                db.collection("users")
                                                        .document(firebaseUser.getUid())
                                                        .set(userMap)
                                                        .addOnSuccessListener(unused -> callback.onSuccess())
                                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                                            }
                                        } else {
                                            callback.onError(authTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        callback.onError("Greška pri proveri korisničkog imena: " + task.getException().getMessage());
                    }
                });
    }


    public interface OnUserRegistered {
        void onSuccess();
        void onError(String message);
    }
}