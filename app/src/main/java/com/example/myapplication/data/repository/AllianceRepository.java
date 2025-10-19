package com.example.myapplication.data.repository;

import com.example.myapplication.data.model.Alliance;
import com.example.myapplication.data.model.AllianceInvitation;
import com.example.myapplication.data.model.ChatMessage;
import com.example.myapplication.data.model.Friend;
import com.example.myapplication.data.model.User;
import com.example.myapplication.helper.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Dodaj prijatelja
    public void addFriend(String currentUserId, String friendUserId, OnOperationComplete callback) {
        db.collection("users").document(friendUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        String avatar = doc.getString("avatar");

                        Map<String, Object> friendData = new HashMap<>();
                        friendData.put("userId", friendUserId);
                        friendData.put("username", username);
                        friendData.put("avatar", avatar);
                        friendData.put("addedTime", System.currentTimeMillis());

                        db.collection("users").document(currentUserId)
                                .collection("friends").document(friendUserId)
                                .set(friendData)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        callback.onError("Korisnik nije pronađen");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Učitaj prijatelje
    public void loadFriends(String userId, OnFriendsLoaded callback) {
        db.collection("users").document(userId).collection("friends")
                .orderBy("addedTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Friend> friends = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Friend friend = doc.toObject(Friend.class);
                        if (friend != null) {
                            friends.add(friend);
                        }
                    }
                    callback.onFriendsLoaded(friends);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Kreiraj savez
    public void createAlliance(String name, String leaderId, String leaderUsername, OnAllianceCreated callback) {
        // Proveri da li korisnik već ima savez
        db.collection("users").document(leaderId).get()
                .addOnSuccessListener(doc -> {
                    String currentAllianceId = doc.getString("currentAllianceId");
                    if (currentAllianceId != null && !currentAllianceId.isEmpty()) {
                        callback.onError("Moraš prvo ukinuti prethodni savez!");
                        return;
                    }

                    String allianceId = db.collection("alliances").document().getId();
                    Alliance alliance = new Alliance(allianceId, name, leaderId, leaderUsername);

                    db.collection("alliances").document(allianceId)
                            .set(alliance)
                            .addOnSuccessListener(aVoid -> {
                                // Postavi savez kao trenutni za vođu
                                db.collection("users").document(leaderId)
                                        .update("currentAllianceId", allianceId)
                                        .addOnSuccessListener(aVoid1 -> callback.onAllianceCreated(allianceId))
                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Pošalji pozivnicu za savez
    public void sendAllianceInvitation(String allianceId, String allianceName, String fromUserId,
                                       String fromUsername, String toUserId, OnOperationComplete callback) {
        String invitationId = db.collection("allianceInvitations").document().getId();
        AllianceInvitation invitation = new AllianceInvitation(
                invitationId, allianceId, allianceName, fromUserId, fromUsername, toUserId
        );

        db.collection("allianceInvitations").document(invitationId)
                .set(invitation)
                .addOnSuccessListener(aVoid -> {
                    // Pošalji notifikaciju
                    NotificationHelper.sendNotificationToUser(
                            toUserId,
                            "Poziv u savez",
                            fromUsername + " te poziva u savez '" + allianceName + "'"
                    );
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Prihvati pozivnicu
    public void acceptInvitation(String invitationId, String allianceId, String userId,
                                 String username, OnOperationComplete callback) {
        // Prvo proveri da li trenutni savez ima aktivnu misiju
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String currentAllianceId = userDoc.getString("currentAllianceId");

                    if (currentAllianceId != null && !currentAllianceId.isEmpty()) {
                        // Proveri da li je misija aktivna
                        db.collection("alliances").document(currentAllianceId).get()
                                .addOnSuccessListener(allianceDoc -> {
                                    Boolean missionActive = allianceDoc.getBoolean("missionActive");
                                    if (missionActive != null && missionActive) {
                                        callback.onError("Ne možeš napustiti savez tokom aktivne misije!");
                                        return;
                                    }

                                    // Ukloni korisnika iz starog saveza
                                    removeUserFromAlliance(currentAllianceId, userId, new OnOperationComplete() {
                                        @Override
                                        public void onSuccess() {
                                            joinNewAlliance(invitationId, allianceId, userId, username, callback);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            callback.onError(message);
                                        }
                                    });
                                });
                    } else {
                        joinNewAlliance(invitationId, allianceId, userId, username, callback);
                    }
                });
    }

    private void joinNewAlliance(String invitationId, String allianceId, String userId,
                                 String username, OnOperationComplete callback) {
        // Dodaj korisnika u savez
        db.collection("alliances").document(allianceId)
                .update(
                        "memberIds", FieldValue.arrayUnion(userId),
                        "memberUsernames", FieldValue.arrayUnion(username)
                )
                .addOnSuccessListener(aVoid -> {
                    // Ažuriraj korisnika
                    db.collection("users").document(userId)
                            .update("currentAllianceId", allianceId)
                            .addOnSuccessListener(aVoid1 -> {
                                // Označi pozivnicu kao prihvaćenu
                                db.collection("allianceInvitations").document(invitationId)
                                        .update("status", "accepted")
                                        .addOnSuccessListener(aVoid2 -> {
                                            // Obavesti vođu
                                            db.collection("alliances").document(allianceId).get()
                                                    .addOnSuccessListener(doc -> {
                                                        String leaderId = doc.getString("leaderId");
                                                        NotificationHelper.sendNotificationToUser(
                                                                leaderId,
                                                                "Novi član saveza",
                                                                username + " je pristupio savezu"
                                                        );
                                                        callback.onSuccess();
                                                    });
                                        });
                            });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Odbij pozivnicu
    public void declineInvitation(String invitationId, OnOperationComplete callback) {
        db.collection("allianceInvitations").document(invitationId)
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Napusti savez
    public void leaveAlliance(String allianceId, String userId, OnOperationComplete callback) {
        // Proveri da li je misija aktivna
        db.collection("alliances").document(allianceId).get()
                .addOnSuccessListener(doc -> {
                    Boolean missionActive = doc.getBoolean("missionActive");
                    if (missionActive != null && missionActive) {
                        callback.onError("Ne možeš napustiti savez tokom aktivne misije!");
                        return;
                    }

                    String leaderId = doc.getString("leaderId");
                    if (leaderId != null && leaderId.equals(userId)) {
                        callback.onError("Vođa ne može napustiti savez! Možeš ga samo ukinuti.");
                        return;
                    }

                    removeUserFromAlliance(allianceId, userId, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void removeUserFromAlliance(String allianceId, String userId, OnOperationComplete callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String username = userDoc.getString("username");

                    db.collection("alliances").document(allianceId)
                            .update(
                                    "memberIds", FieldValue.arrayRemove(userId),
                                    "memberUsernames", FieldValue.arrayRemove(username)
                            )
                            .addOnSuccessListener(aVoid -> {
                                db.collection("users").document(userId)
                                        .update("currentAllianceId", "")
                                        .addOnSuccessListener(aVoid1 -> callback.onSuccess())
                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                });
    }

    // Ukini savez (samo vođa)
    public void deleteAlliance(String allianceId, String userId, OnOperationComplete callback) {
        db.collection("alliances").document(allianceId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onError("Savez ne postoji");
                        return;
                    }

                    String leaderId = doc.getString("leaderId");
                    if (!leaderId.equals(userId)) {
                        callback.onError("Samo vođa može ukinuti savez");
                        return;
                    }

                    Boolean missionActive = doc.getBoolean("missionActive");
                    if (missionActive != null && missionActive) {
                        callback.onError("Ne možeš ukinuti savez tokom aktivne misije!");
                        return;
                    }

                    List<String> memberIds = (List<String>) doc.get("memberIds");
                    if (memberIds != null) {
                        for (String memberId : memberIds) {
                            db.collection("users").document(memberId)
                                    .update("currentAllianceId", "");
                        }
                    }

                    db.collection("alliances").document(allianceId).delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Učitaj trenutni savez
    public void loadCurrentAlliance(String userId, OnAllianceLoaded callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    String allianceId = doc.getString("currentAllianceId");
                    if (allianceId == null || allianceId.isEmpty()) {
                        callback.onAllianceLoaded(null);
                        return;
                    }

                    db.collection("alliances").document(allianceId).get()
                            .addOnSuccessListener(allianceDoc -> {
                                if (allianceDoc.exists()) {
                                    Alliance alliance = allianceDoc.toObject(Alliance.class);
                                    callback.onAllianceLoaded(alliance);
                                } else {
                                    callback.onAllianceLoaded(null);
                                }
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Učitaj pozivnice
    public ListenerRegistration listenToInvitations(String userId, OnInvitationsLoaded callback) {
        return db.collection("allianceInvitations")
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }

                    List<AllianceInvitation> invitations = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            AllianceInvitation invitation = doc.toObject(AllianceInvitation.class);
                            if (invitation != null) {
                                invitations.add(invitation);
                            }
                        }
                    }
                    callback.onInvitationsLoaded(invitations);
                });
    }

    // Pošalji poruku u čet
    public void sendMessage(String allianceId, String senderId, String senderUsername,
                            String message, OnOperationComplete callback) {
        String messageId = db.collection("alliances").document(allianceId)
                .collection("messages").document().getId();

        ChatMessage chatMessage = new ChatMessage(messageId, allianceId, senderId, senderUsername, message);

        db.collection("alliances").document(allianceId).collection("messages")
                .document(messageId)
                .set(chatMessage)
                .addOnSuccessListener(aVoid -> {
                    // Obavesti ostale članove
                    db.collection("alliances").document(allianceId).get()
                            .addOnSuccessListener(doc -> {
                                List<String> memberIds = (List<String>) doc.get("memberIds");
                                if (memberIds != null) {
                                    for (String memberId : memberIds) {
                                        if (!memberId.equals(senderId)) {
                                            NotificationHelper.sendNotificationToUser(
                                                    memberId,
                                                    "Nova poruka u savezu",
                                                    senderUsername + ": " + message
                                            );
                                        }
                                    }
                                }
                                callback.onSuccess();
                            });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Slušaj poruke
    public ListenerRegistration listenToMessages(String allianceId, OnMessagesLoaded callback) {
        return db.collection("alliances").document(allianceId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }

                    List<ChatMessage> messages = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                    }
                    callback.onMessagesLoaded(messages);
                });
    }

    // Pokreni misiju (samo vođa)
    public void startMission(String allianceId, String userId, OnOperationComplete callback) {
        db.collection("alliances").document(allianceId).get()
                .addOnSuccessListener(doc -> {
                    String leaderId = doc.getString("leaderId");
                    if (!leaderId.equals(userId)) {
                        callback.onError("Samo vođa može pokrenuti misiju!");
                        return;
                    }

                    Boolean missionActive = doc.getBoolean("missionActive");
                    if (missionActive != null && missionActive) {
                        callback.onError("Misija je već aktivna!");
                        return;
                    }

                    long missionEndTime = System.currentTimeMillis() + 60000; // 1 minut

                    db.collection("alliances").document(allianceId)
                            .update(
                                    "missionActive", true,
                                    "missionEndTime", missionEndTime
                            )
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Pretraži korisnike
    public void searchUsers(String query, OnUsersFound callback) {
        db.collection("users")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    callback.onUsersFound(users);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ============= INTERFEJSI =============

    // Jednostavne operacije
    public interface OnOperationComplete {
        void onSuccess();
        void onError(String message);
    }

    // Učitavanje prijatelja
    public interface OnFriendsLoaded {
        void onFriendsLoaded(List<Friend> friends);
        void onError(String message);
    }

    // Kreiranje saveza
    public interface OnAllianceCreated {
        void onAllianceCreated(String allianceId);
        void onError(String message);
    }

    // Učitavanje saveza
    public interface OnAllianceLoaded {
        void onAllianceLoaded(Alliance alliance);
        void onError(String message);
    }

    // Učitavanje pozivnica
    public interface OnInvitationsLoaded {
        void onInvitationsLoaded(List<AllianceInvitation> invitations);
        void onError(String message);
    }

    // Učitavanje poruka
    public interface OnMessagesLoaded {
        void onMessagesLoaded(List<ChatMessage> messages);
        void onError(String message);
    }

    // Pretraga korisnika
    public interface OnUsersFound {
        void onUsersFound(List<User> users);
        void onError(String message);
    }
}
