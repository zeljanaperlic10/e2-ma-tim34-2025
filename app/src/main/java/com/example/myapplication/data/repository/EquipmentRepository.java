package com.example.myapplication.data.repository;

import com.example.myapplication.data.model.Equipment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EquipmentRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addEquipment(Equipment equipment, OnOperationComplete callback) {
        db.collection("equipment")
                .add(equipment)
                .addOnSuccessListener(ref -> {
                    equipment.setFirestoreId(ref.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getEquipmentForUser(String userId, OnEquipmentLoaded callback) {
        db.collection("equipment")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Equipment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Equipment eq = doc.toObject(Equipment.class);
                        eq.setFirestoreId(doc.getId());
                        list.add(eq);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateEquipment(Equipment equipment, OnOperationComplete callback) {
        db.collection("equipment").document(equipment.getFirestoreId())
                .set(equipment)
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteEquipment(String firestoreId, OnOperationComplete callback) {
        db.collection("equipment").document(firestoreId)
                .delete()
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface OnOperationComplete {
        void onSuccess();
        void onError(String message);
    }

    public interface OnEquipmentLoaded {
        void onSuccess(List<Equipment> equipment);
        void onError(String message);
    }
}