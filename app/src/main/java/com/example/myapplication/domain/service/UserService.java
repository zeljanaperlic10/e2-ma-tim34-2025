package com.example.myapplication.domain.service;

import com.example.myapplication.data.model.User;
import com.example.myapplication.data.repository.FirebaseRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserService {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void register(String email, String password, String username, String avatar,
                         FirebaseRepository.OnUserRegistered callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();

                        // Pošalji verifikacioni email
                        firebaseUser.sendEmailVerification();

                        // Kreiraj User objekat sa SVIM poljima
                        User newUser = new User(uid, username, email, avatar, false, System.currentTimeMillis());

                        // DODATO: Postavi default vrednosti za napredovanje
                        newUser.setLevel(0);
                        newUser.setXp(0);
                        newUser.setRequiredXp(200);
                        newUser.setPp(40);
                        newUser.setCoins(0);
                        newUser.setTitle("Početnik");
                        newUser.setLastLevelUpTimestamp(System.currentTimeMillis());

                        // Sačuvaj u Firestore sa SVIM poljima
                        db.collection("users").document(uid)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}