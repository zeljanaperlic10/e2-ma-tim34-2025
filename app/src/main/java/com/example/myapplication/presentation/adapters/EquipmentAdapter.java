package com.example.myapplication.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Equipment;

import java.util.ArrayList;
import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private List<Equipment> equipmentList;
    private List<Equipment> selectedEquipment = new ArrayList<>();
    private Context context;

    public EquipmentAdapter(Context context, List<Equipment> equipmentList) {
        this.context = context;
        this.equipmentList = equipmentList;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);

        holder.tvEquipmentName.setText(equipment.getName());
        holder.tvEquipmentType.setText(equipment.getType().equals("WEAPON") ? "Oružje" : "Odeća");

        // Prikaz bonusa
        String bonus = "";
        if (equipment.getBonusPp() > 0) bonus += "+" + equipment.getBonusPp() + " PP ";
        if (equipment.getBonusSuccessChance() > 0) bonus += "+" + equipment.getBonusSuccessChance() + "% šanse ";
        if (equipment.getBonusAttacks() > 0) bonus += "+" + equipment.getBonusAttacks() + " napada";
        holder.tvEquipmentBonus.setText(bonus.trim());

        // Trajanje (samo za odeću)
        if (equipment.getType().equals("CLOTHING") && equipment.getRemainingBattles() > 0) {
            holder.tvEquipmentDurability.setVisibility(View.VISIBLE);
            holder.tvEquipmentDurability.setText("Trajanje: " + equipment.getRemainingBattles() + " borbe");
        } else if (equipment.getType().equals("WEAPON")) {
            holder.tvEquipmentDurability.setVisibility(View.VISIBLE);
            holder.tvEquipmentDurability.setText("Permanentno");
        } else {
            holder.tvEquipmentDurability.setVisibility(View.GONE);
        }

        holder.cbEquipment.setChecked(selectedEquipment.contains(equipment));

        holder.cbEquipment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedEquipment.contains(equipment)) {
                    selectedEquipment.add(equipment);
                }
            } else {
                selectedEquipment.remove(equipment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public List<Equipment> getSelectedEquipment() {
        return selectedEquipment;
    }

    public void updateList(List<Equipment> newList) {
        this.equipmentList = newList;
        notifyDataSetChanged();
    }

    public static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbEquipment;
        TextView tvEquipmentName, tvEquipmentType, tvEquipmentBonus, tvEquipmentDurability;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cbEquipment = itemView.findViewById(R.id.cbEquipment);
            tvEquipmentName = itemView.findViewById(R.id.tvEquipmentName);
            tvEquipmentType = itemView.findViewById(R.id.tvEquipmentType);
            tvEquipmentBonus = itemView.findViewById(R.id.tvEquipmentBonus);
            tvEquipmentDurability = itemView.findViewById(R.id.tvEquipmentDurability);
        }
    }
}
