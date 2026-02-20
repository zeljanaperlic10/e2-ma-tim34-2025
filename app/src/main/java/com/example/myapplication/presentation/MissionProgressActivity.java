package com.example.myapplication.presentation;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.MissionContribution;
import com.example.myapplication.data.repository.AllianceRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MissionProgressActivity extends AppCompatActivity {

    private TextView tvBossHp, tvAllianceTotal;
    private ProgressBar progressBossHp;
    private RecyclerView recyclerMembers;
    private MissionMembersAdapter membersAdapter;

    private AllianceRepository repository;
    private String allianceId;
    private ListenerRegistration allianceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_progress);

        allianceId = getIntent().getStringExtra("ALLIANCE_ID");
        repository = new AllianceRepository();

        tvBossHp        = findViewById(R.id.tvBossHp);
        tvAllianceTotal = findViewById(R.id.tvAllianceTotal);
        progressBossHp  = findViewById(R.id.progressBossHp);
        recyclerMembers = findViewById(R.id.recyclerMembers);

        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MissionMembersAdapter();
        recyclerMembers.setAdapter(membersAdapter);

        listenToAllianceHp();
        loadContributions();
    }

    private void listenToAllianceHp() {
        allianceListener = FirebaseFirestore.getInstance()
                .collection("alliances").document(allianceId)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null || !snap.exists()) return;
                    Long maxHp = snap.getLong("missionBossMaxHp");
                    Long curHp = snap.getLong("missionBossCurrentHp");
                    if (maxHp != null && curHp != null) {
                        tvBossHp.setText("Bos HP: " + curHp + " / " + maxHp);
                        progressBossHp.setMax(maxHp.intValue());
                        progressBossHp.setProgress(curHp.intValue());
                    }
                });
    }

    private void loadContributions() {
        repository.getAllContributions(allianceId, new AllianceRepository.OnAllContributionsLoaded() {
            @Override
            public void onLoaded(List<MissionContribution> contributions) {
                membersAdapter.setData(contributions);

                int total = 0;
                for (MissionContribution c : contributions) total += c.calculateCurrentHp();
                tvAllianceTotal.setText("Ukupan doprinos saveza: " + total + " HP");
            }
            @Override
            public void onError(String message) {
                Toast.makeText(MissionProgressActivity.this, "Greška: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (allianceListener != null) allianceListener.remove();
    }

    // ===== Interni adapter =====

    private static class MissionMembersAdapter extends RecyclerView.Adapter<MissionMembersAdapter.VH> {
        private List<MissionContribution> data;

        void setData(List<MissionContribution> list) {
            this.data = list;
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mission_member, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            MissionContribution c = data.get(position);
            holder.tvName.setText(c.getUsername() != null ? c.getUsername() : "Korisnik");
            int hp = c.calculateCurrentHp();
            // Dodaj bonus za "bez neurađenih" ako ga ima
            if (!c.isHasUndoneTask()) hp += 10;
            holder.tvHp.setText("Doprinos: " + hp + " HP");
            holder.tvDetails.setText(
                    "Kupovine: " + c.getStorePurchases() + "/5  " +
                            "Udarca: " + c.getSuccessfulHits() + "/10  " +
                            "Ostali zadaci: " + c.getOtherTasksCount() + "/6  " +
                            "Dani poruka: " + (c.getMessageDays() != null ? c.getMessageDays().size() : 0) +
                            (c.isHasUndoneTask() ? "  ❌neurađen" : "  ✅bez neurađenih"));
        }

        @Override
        public int getItemCount() { return data == null ? 0 : data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvHp, tvDetails;
            VH(android.view.View v) {
                super(v);
                tvName    = v.findViewById(R.id.tvMemberName);
                tvHp      = v.findViewById(R.id.tvMemberHp);
                tvDetails = v.findViewById(R.id.tvMemberDetails);
            }
        }
    }
}
