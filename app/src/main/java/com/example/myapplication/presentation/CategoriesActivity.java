package com.example.myapplication.presentation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Category;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.presentation.adapters.CategoryAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerCategories;
    private TextView tvEmptyCategories;
    private Button btnAddCategory;

    private CategoryAdapter categoryAdapter;
    private CategoryRepository categoryRepository = new CategoryRepository();
    private String userId;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerCategories = findViewById(R.id.recyclerCategories);
        tvEmptyCategories = findViewById(R.id.tvEmptyCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categoryList, category -> {
            showColorPickerDialog(category);
        });
        recyclerCategories.setAdapter(categoryAdapter);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        loadCategories();
    }

    private void loadCategories() {
        categoryRepository.getCategories(userId, new CategoryRepository.OnCategoriesLoaded() {
            @Override
            public void onSuccess(List<Category> categories) {
                categoryList = categories;
                categoryAdapter.updateList(categoryList);

                if (categoryList.isEmpty()) {
                    recyclerCategories.setVisibility(View.GONE);
                    tvEmptyCategories.setVisibility(View.VISIBLE);
                } else {
                    recyclerCategories.setVisibility(View.VISIBLE);
                    tvEmptyCategories.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CategoriesActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nova kategorija");

        EditText input = new EditText(this);
        input.setHint("Unesi naziv kategorije");
        builder.setView(input);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Naziv ne može biti prazan!", Toast.LENGTH_SHORT).show();
                return;
            }

            int randomColor = generateRandomColor();
            Category category = new Category(name, randomColor, userId);

            categoryRepository.addCategory(category, new CategoryRepository.OnOperationComplete() {
                @Override
                public void onSuccess() {
                    Toast.makeText(CategoriesActivity.this, "Kategorija dodata!", Toast.LENGTH_SHORT).show();
                    loadCategories();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(CategoriesActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }

    private void showColorPickerDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Promeni boju za: " + category.getName());

        String[] colorNames = {"Crvena", "Plava", "Zelena", "Žuta", "Ljubičasta", "Narandžasta", "Roza", "Tirkizna"};
        int[] colors = {
                Color.parseColor("#F44336"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#00BCD4")
        };

        builder.setItems(colorNames, (dialog, which) -> {
            int newColor = colors[which];

            categoryRepository.updateCategoryColor(category.getFirestoreId(), newColor, userId, category.getName(),
                    new CategoryRepository.OnOperationComplete() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(CategoriesActivity.this, "Boja promenjena!", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(CategoriesActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }

    private int generateRandomColor() {
        Random random = new Random();
        int[] colors = {
                Color.parseColor("#F44336"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#00BCD4")
        };
        return colors[random.nextInt(colors.length)];
    }
}