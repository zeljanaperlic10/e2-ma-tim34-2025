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
import com.example.myapplication.presentation.CalendarActivity;
import com.example.myapplication.presentation.CategoriesActivity;
import com.example.myapplication.presentation.EquipmentSelectorActivity;
import com.example.myapplication.presentation.StatsActivity;
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

        Button btnOpenCalendar = view.findViewById(R.id.btnOpenCalendar);
        btnOpenCalendar.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CalendarActivity.class));
        });

        Button btnOpenStats = view.findViewById(R.id.btnOpenStats);
        btnOpenStats.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), StatsActivity.class));
        });

        Button btnOpenBossFight = view.findViewById(R.id.btnOpenBossFight);
        btnOpenBossFight.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EquipmentSelectorActivity.class));
        });

        return view;
    }
}