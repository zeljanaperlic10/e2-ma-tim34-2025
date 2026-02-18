package com.example.myapplication.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Boss;

import java.util.List;

public class StatsBossAdapter extends RecyclerView.Adapter<StatsBossAdapter.StatsBossViewHolder> {

    private List<Boss> bossList;
    private Context context;

    public StatsBossAdapter(Context context, List<Boss> bossList) {
        this.context = context;
        this.bossList = bossList;
    }

    @NonNull
    @Override
    public StatsBossViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stats_boss, parent, false);
        return new StatsBossViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsBossViewHolder holder, int position) {
        Boss boss = bossList.get(position);

        holder.tvStatsBossLevel.setText("Boss Nivo " + boss.getLevel());
        holder.tvStatsBossHp.setText("HP: " + boss.getMaxHp());

        if (boss.isDefeated()) {
            holder.tvStatsBossStatus.setText("✓ POBEDA");
            holder.tvStatsBossStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
        } else {
            holder.tvStatsBossStatus.setText("✗ NEPORAŽEN");
            holder.tvStatsBossStatus.setBackgroundColor(android.graphics.Color.parseColor("#F44336"));
        }
    }

    @Override
    public int getItemCount() {
        return bossList.size();
    }

    public static class StatsBossViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatsBossIcon, tvStatsBossLevel, tvStatsBossHp, tvStatsBossStatus;

        public StatsBossViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatsBossIcon = itemView.findViewById(R.id.tvStatsBossIcon);
            tvStatsBossLevel = itemView.findViewById(R.id.tvStatsBossLevel);
            tvStatsBossHp = itemView.findViewById(R.id.tvStatsBossHp);
            tvStatsBossStatus = itemView.findViewById(R.id.tvStatsBossStatus);
        }
    }
}
