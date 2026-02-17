package com.example.myapplication.data.repository;

import com.example.myapplication.data.model.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addCategory(Category category, OnOperationComplete callback) {
        // Proveri da li već postoji kategorija sa istom bojom
        db.collection("categories")
                .whereEqualTo("userId", category.getUserId())
                .whereEqualTo("color", category.getColor())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        callback.onError("Već postoji kategorija sa ovom bojom!");
                        return;
                    }
                    db.collection("categories")
                            .add(category)
                            .addOnSuccessListener(ref -> {
                                category.setFirestoreId(ref.getId());
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getCategories(String userId, OnCategoriesLoaded callback) {
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Category> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Category c = doc.toObject(Category.class);
                        c.setFirestoreId(doc.getId());
                        list.add(c);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateCategoryColor(String firestoreId, int newColor, String userId, OnOperationComplete callback) {
        // Proveri da li već postoji kategorija sa novom bojom
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .whereEqualTo("color", newColor)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        callback.onError("Već postoji kategorija sa ovom bojom!");
                        return;
                    }
                    db.collection("categories").document(firestoreId)
                            .update("color", newColor)
                            .addOnSuccessListener(a -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteCategory(String firestoreId, OnOperationComplete callback) {
        db.collection("categories").document(firestoreId)
                .delete()
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface OnOperationComplete {
        void onSuccess();
        void onError(String message);
    }

    public interface OnCategoriesLoaded {
        void onSuccess(List<Category> categories);
        void onError(String message);
    }
}
