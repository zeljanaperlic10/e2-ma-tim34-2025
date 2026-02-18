package com.example.myapplication.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Equipment;

import java.util.List;

public class StatsEquipmentAdapter extends RecyclerView.Adapter<StatsEquipmentAdapter.StatsEquipmentViewHolder> {

    private List<Equipment> equipmentList;
    private Context context;

    public StatsEquipmentAdapter(Context context, List<Equipment> equipmentList) {
        this.context = context;
        this.equipmentList = equipmentList;
    }

    @NonNull
    @Override
    public StatsEquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stats_equipment, parent, false);
        return new StatsEquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsEquipmentViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);

        holder.tvStatsEquipName.setText(equipment.getName());

        String icon = equipment.getType().equals("WEAPON") ? "⚔️" : "🛡️";
        holder.tvStatsEquipIcon.setText(icon);

        String typeText = equipment.getType().equals("WEAPON") ? "Oružje • Permanentno" :
                "Odeća • " + equipment.getRemainingBattles() + " borbe";
        holder.tvStatsEquipType.setText(typeText);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public static class StatsEquipmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatsEquipIcon, tvStatsEquipName, tvStatsEquipType;

        public StatsEquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatsEquipIcon = itemView.findViewById(R.id.tvStatsEquipIcon);
            tvStatsEquipName = itemView.findViewById(R.id.tvStatsEquipName);
            tvStatsEquipType = itemView.findViewById(R.id.tvStatsEquipType);
        }
    }
}
