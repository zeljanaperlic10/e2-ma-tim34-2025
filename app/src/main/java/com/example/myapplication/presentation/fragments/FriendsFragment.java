package com.example.myapplication.presentation.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Alliance;
import com.example.myapplication.data.model.AllianceInvitation;
import com.example.myapplication.data.model.Friend;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.repository.AllianceRepository;
import com.example.myapplication.presentation.AllianceActivity;
import com.example.myapplication.presentation.adapters.FriendsAdapter;
import com.example.myapplication.presentation.adapters.InviteFriendsAdapter;
import com.example.myapplication.presentation.adapters.UserSearchAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private Button searchUserBtn, scanQrBtn, createAllianceBtn, viewAllianceBtn;
    private TextView allianceStatusText;
    private AllianceRepository repository;
    private String currentUserId;
    private ListenerRegistration invitationListener;
    private List<AllianceInvitation> pendingInvitations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        repository = new AllianceRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView);
        searchUserBtn = view.findViewById(R.id.searchUserBtn);
        scanQrBtn = view.findViewById(R.id.scanQrBtn);
        createAllianceBtn = view.findViewById(R.id.createAllianceBtn);
        viewAllianceBtn = view.findViewById(R.id.viewAllianceBtn);
        allianceStatusText = view.findViewById(R.id.allianceStatusText);

        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsAdapter = new FriendsAdapter(new ArrayList<>(), this::onFriendClick);
        friendsRecyclerView.setAdapter(friendsAdapter);

        searchUserBtn.setOnClickListener(v -> showSearchDialog());
        scanQrBtn.setOnClickListener(v -> startQRScanner());
        createAllianceBtn.setOnClickListener(v -> showCreateAllianceDialog());
        viewAllianceBtn.setOnClickListener(v -> openAllianceActivity());

        loadFriends();
        loadAllianceStatus();
        listenForInvitations();

        return view;
    }

    private void loadFriends() {
        repository.loadFriends(currentUserId, new AllianceRepository.OnFriendsLoaded() {
            @Override
            public void onFriendsLoaded(List<Friend> friends) {
                friendsAdapter.updateFriends(friends);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllianceStatus() {
        repository.loadCurrentAlliance(currentUserId, new AllianceRepository.OnAllianceLoaded() {
            @Override
            public void onAllianceLoaded(Alliance alliance) {
                if (alliance != null) {
                    allianceStatusText.setText("Savez: " + alliance.getName());
                    viewAllianceBtn.setVisibility(View.VISIBLE);
                    createAllianceBtn.setText("Ukini savez");
                } else {
                    allianceStatusText.setText("Nisi u savezu");
                    viewAllianceBtn.setVisibility(View.GONE);
                    createAllianceBtn.setText("Kreiraj savez");
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForInvitations() {
        invitationListener = repository.listenToInvitations(currentUserId,
                new AllianceRepository.OnInvitationsLoaded() {
                    @Override
                    public void onInvitationsLoaded(List<AllianceInvitation> invitations) {
                        pendingInvitations = invitations;
                        if (!invitations.isEmpty()) {
                            showInvitationDialog(invitations.get(0));
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search_user, null);
        EditText searchInput = dialogView.findViewById(R.id.searchInput);
        Button searchBtn = dialogView.findViewById(R.id.searchBtn);
        RecyclerView searchResultsRecycler = dialogView.findViewById(R.id.searchResultsRecycler);

        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        UserSearchAdapter searchAdapter = new UserSearchAdapter(new ArrayList<>(), this::addFriendById);
        searchResultsRecycler.setAdapter(searchAdapter);

        AlertDialog dialog = builder.setView(dialogView)
                .setNegativeButton("Zatvori", null)
                .create();

        searchBtn.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                repository.searchUsers(query, new AllianceRepository.OnUsersFound() {
                    @Override
                    public void onUsersFound(List<User> users) {
                        searchAdapter.updateUsers(users);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void startQRScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Skeniraj QR kod prijatelja");
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String friendUserId = result.getContents();
            addFriendById(friendUserId);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addFriendById(String friendUserId) {
        if (friendUserId.equals(currentUserId)) {
            Toast.makeText(getContext(), "Ne možeš dodati sebe!", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.addFriend(currentUserId, friendUserId, new AllianceRepository.OnOperationComplete() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Prijatelj dodat!", Toast.LENGTH_SHORT).show();
                loadFriends();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateAllianceDialog() {
        repository.loadCurrentAlliance(currentUserId, new AllianceRepository.OnAllianceLoaded() {
            @Override
            public void onAllianceLoaded(Alliance alliance) {
                if (alliance != null) {
                    // Ukini savez
                    if (alliance.getLeaderId().equals(currentUserId)) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Ukini savez")
                                .setMessage("Da li si siguran da želiš da ukinuš savez '" + alliance.getName() + "'?")
                                .setPositiveButton("Da", (dialog, which) -> {
                                    repository.deleteAlliance(alliance.getId(), currentUserId,
                                            new AllianceRepository.OnOperationComplete() {
                                                @Override
                                                public void onSuccess() {
                                                    Toast.makeText(getContext(), "Savez ukinut!", Toast.LENGTH_SHORT).show();
                                                    loadAllianceStatus();
                                                }

                                                @Override
                                                public void onError(String message) {
                                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                })
                                .setNegativeButton("Ne", null)
                                .show();
                    } else {
                        Toast.makeText(getContext(), "Samo vođa može ukinuti savez", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Kreiraj novi savez
                    showCreateAllianceInputDialog();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateAllianceInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_alliance, null);
        EditText allianceNameInput = dialogView.findViewById(R.id.allianceNameInput);

        builder.setView(dialogView)
                .setTitle("Kreiraj savez")
                .setPositiveButton("Kreiraj", (dialog, which) -> {
                    String name = allianceNameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Unesi naziv saveza!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseAuth.getInstance().getCurrentUser();
                    String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    // Ako display name nije postavljen, učitaj iz Firestore
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(currentUserId).get()
                            .addOnSuccessListener(doc -> {
                                String uname = doc.getString("username");
                                repository.createAlliance(name, currentUserId, uname,
                                        new AllianceRepository.OnAllianceCreated() {
                                            @Override
                                            public void onAllianceCreated(String allianceId) {
                                                Toast.makeText(getContext(), "Savez kreiran!", Toast.LENGTH_SHORT).show();
                                                loadAllianceStatus();
                                                showInviteFriendsDialog(allianceId, name);
                                            }

                                            @Override
                                            public void onError(String message) {
                                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                })
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void showInviteFriendsDialog(String allianceId, String allianceName) {
        repository.loadFriends(currentUserId, new AllianceRepository.OnFriendsLoaded() {
            @Override
            public void onFriendsLoaded(List<Friend> friends) {
                if (friends.isEmpty()) {
                    Toast.makeText(getContext(), "Nemaš prijatelja za pozivanje", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_friends, null);
                RecyclerView friendsRecycler = dialogView.findViewById(R.id.inviteFriendsRecycler);

                friendsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                InviteFriendsAdapter inviteAdapter = new InviteFriendsAdapter(friends,
                        friend -> inviteFriend(allianceId, allianceName, friend));
                friendsRecycler.setAdapter(inviteAdapter);

                builder.setView(dialogView)
                        .setTitle("Pozovi prijatelje")
                        .setNegativeButton("Zatvori", null)
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inviteFriend(String allianceId, String allianceName, Friend friend) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");
                    repository.sendAllianceInvitation(allianceId, allianceName, currentUserId,
                            username, friend.getUserId(), new AllianceRepository.OnOperationComplete() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Poziv poslat!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    private void showInvitationDialog(AllianceInvitation invitation) {
        new AlertDialog.Builder(getContext())
                .setTitle("Poziv u savez")
                .setMessage(invitation.getFromUsername() + " te poziva u savez '" +
                        invitation.getAllianceName() + "'")
                .setPositiveButton("Prihvati", (dialog, which) -> {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(currentUserId).get()
                            .addOnSuccessListener(doc -> {
                                String username = doc.getString("username");
                                repository.acceptInvitation(invitation.getId(), invitation.getAllianceId(),
                                        currentUserId, username, new AllianceRepository.OnOperationComplete() {
                                            @Override
                                            public void onSuccess() {
                                                Toast.makeText(getContext(), "Pristupio si savezu!", Toast.LENGTH_SHORT).show();
                                                loadAllianceStatus();
                                            }

                                            @Override
                                            public void onError(String message) {
                                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                })
                .setNegativeButton("Odbij", (dialog, which) -> {
                    repository.declineInvitation(invitation.getId(),
                            new AllianceRepository.OnOperationComplete() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Poziv odbijen", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setCancelable(false)
                .show();
    }

    private void onFriendClick(Friend friend) {
        // Opciono: Prikaži profil prijatelja
    }

    private void openAllianceActivity() {
        Intent intent = new Intent(getActivity(), AllianceActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (invitationListener != null) {
            invitationListener.remove();
        }
    }
}
