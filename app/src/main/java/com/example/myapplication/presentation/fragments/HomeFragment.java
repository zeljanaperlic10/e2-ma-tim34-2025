package com.example.myapplication.presentation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.presentation.CategoriesActivity;
import com.example.myapplication.presentation.TaskListActivity;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnOpenTasks = view.findViewById(R.id.btnOpenTasks);
        btnOpenTasks.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TaskListActivity.class));
        });

        Button btnOpenCategories = view.findViewById(R.id.btnOpenCategories);
        btnOpenCategories.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CategoriesActivity.class));
        });

        return view;
    }
}
