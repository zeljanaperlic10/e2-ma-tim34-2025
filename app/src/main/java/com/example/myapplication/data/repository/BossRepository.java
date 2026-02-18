package com.example.myapplication.data.repository;

import com.example.myapplication.data.model.Boss;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BossRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void createBoss(Boss boss, OnOperationComplete callback) {
        db.collection("bosses")
                .add(boss)
                .addOnSuccessListener(ref -> {
                    boss.setFirestoreId(ref.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getBossesForUser(String userId, OnBossesLoaded callback) {
        db.collection("bosses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Boss> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Boss b = doc.toObject(Boss.class);
                        b.setFirestoreId(doc.getId());
                        list.add(b);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateBoss(Boss boss, OnOperationComplete callback) {
        db.collection("bosses").document(boss.getFirestoreId())
                .set(boss)
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteBoss(String firestoreId, OnOperationComplete callback) {
        db.collection("bosses").document(firestoreId)
                .delete()
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface OnOperationComplete {
        void onSuccess();
        void onError(String message);
    }

    public interface OnBossesLoaded {
        void onSuccess(List<Boss> bosses);
        void onError(String message);
    }
}
