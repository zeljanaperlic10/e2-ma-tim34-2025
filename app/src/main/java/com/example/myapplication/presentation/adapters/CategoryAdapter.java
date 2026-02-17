package com.example.myapplication.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onChangeColorClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryActionListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());
        holder.viewCategoryColorPreview.setBackgroundColor(category.getColor());

        holder.btnChangeColor.setOnClickListener(v -> listener.onChangeColorClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateList(List<Category> newList) {
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View viewCategoryColorPreview;
        TextView tvCategoryName;
        Button btnChangeColor;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryColorPreview = itemView.findViewById(R.id.viewCategoryColorPreview);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnChangeColor = itemView.findViewById(R.id.btnChangeColor);
        }
    }
}