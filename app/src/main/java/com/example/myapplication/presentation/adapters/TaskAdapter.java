package com.example.myapplication.presentation.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.model.Task;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Context context;
    private OnTaskClickListener listener;

    // Interfejs za klik na zadatak
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    // Kreira view za jedan red u listi
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    // Puni jedan red podacima iz Task objekta
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Postavi naziv
        holder.tvTaskName.setText(task.getName());

        // Postavi kategoriju
        holder.tvTaskCategory.setText(task.getCategory());

        // Postavi boju kategorije na traku
        holder.viewCategoryColor.setBackgroundColor(task.getCategoryColor());

        // Postavi datum
        if (task.getStartDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            holder.tvTaskDate.setText(sdf.format(new Date(task.getStartDate())));
        }

        // Postavi XP
        holder.tvTaskXP.setText("XP: " + task.getTotalXP());

        // Postavi status i boju statusa
        String status = task.getStatus();
        if (status == null) status = "ACTIVE";

        switch (status) {
            case "ACTIVE":
                holder.tvTaskStatus.setText("AKTIVAN");
                holder.tvTaskStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // zelena
                break;
            case "DONE":
                holder.tvTaskStatus.setText("URAĐEN");
                holder.tvTaskStatus.setBackgroundColor(Color.parseColor("#2196F3")); // plava
                break;
            case "UNDONE":
                holder.tvTaskStatus.setText("NEURAĐEN");
                holder.tvTaskStatus.setBackgroundColor(Color.parseColor("#9E9E9E")); // siva
                break;
            case "PAUSED":
                holder.tvTaskStatus.setText("PAUZIRAN");
                holder.tvTaskStatus.setBackgroundColor(Color.parseColor("#FF9800")); // narandžasta
                break;
            case "CANCELLED":
                holder.tvTaskStatus.setText("OTKAZAN");
                holder.tvTaskStatus.setBackgroundColor(Color.parseColor("#F44336")); // crvena
                break;
        }

        // Klik na zadatak otvara detalje
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Ažuriranje liste
    public void updateList(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder — drži reference na view elemente iz item_task.xml
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        View viewCategoryColor;
        TextView tvTaskName, tvTaskCategory, tvTaskDate, tvTaskXP, tvTaskStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskCategory = itemView.findViewById(R.id.tvTaskCategory);
            tvTaskDate = itemView.findViewById(R.id.tvTaskDate);
            tvTaskXP = itemView.findViewById(R.id.tvTaskXP);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
        }
    }
}
