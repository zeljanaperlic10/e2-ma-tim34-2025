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
                                    if (alliance != null) alliance.setId(allianceDoc.getId()); // Firestore ID
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

    // ============= SPECIJALNA MISIJA =============

    /** Pokreni misiju sa HP = 100 * broj članova, trajanje 2 min (demo umesto 2 nedelje). */
    public void startMissionWithBoss(String allianceId, String userId, int memberCount, OnOperationComplete callback) {
        db.collection("alliances").document(allianceId).get()
                .addOnSuccessListener(doc -> {
                    String leaderId = doc.getString("leaderId");
                    if (!leaderId.equals(userId)) { callback.onError("Samo vođa može pokrenuti misiju!"); return; }
                    Boolean missionActive = doc.getBoolean("missionActive");
                    if (missionActive != null && missionActive) { callback.onError("Misija je već aktivna!"); return; }

                    int bossMaxHp = 100; // TEST: fiksno 100 HP, vrati na (100 * memberCount) pre odbrane
                    long missionEndTime = System.currentTimeMillis() + 10L * 60 * 1000; // 10 min demo

                    db.collection("alliances").document(allianceId)
                            .update("missionActive", true,
                                    "missionEndTime", missionEndTime,
                                    "missionBossMaxHp", bossMaxHp,
                                    "missionBossCurrentHp", bossMaxHp,
                                    "missionStartTime", System.currentTimeMillis())
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Učitaj doprinos jednog korisnika. */
    public void getMissionContribution(String allianceId, String userId, OnMissionContributionLoaded callback) {
        db.collection("alliances").document(allianceId)
                .collection("contributions").document(userId).get()
                .addOnSuccessListener(doc -> {
                    com.example.myapplication.data.model.MissionContribution c =
                            doc.exists() ? doc.toObject(com.example.myapplication.data.model.MissionContribution.class) : null;
                    callback.onLoaded(c);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Učitaj doprinose svih članova. */
    public void getAllContributions(String allianceId, OnAllContributionsLoaded callback) {
        db.collection("alliances").document(allianceId).collection("contributions").get()
                .addOnSuccessListener(snap -> {
                    List<com.example.myapplication.data.model.MissionContribution> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        com.example.myapplication.data.model.MissionContribution mc =
                                d.toObject(com.example.myapplication.data.model.MissionContribution.class);
                        if (mc != null) list.add(mc);
                    }
                    callback.onLoaded(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Registruj kupovinu u prodavnici (max 5, po 2 HP). */
    public void registerStorePurchase(String allianceId, String userId, String username, OnHpReduced callback) {
        loadOrCreateContrib(allianceId, userId, username, contrib -> {
            if (contrib.getStorePurchases() >= 5) { callback.onAlreadyMaxed(); return; }
            contrib.setStorePurchases(contrib.getStorePurchases() + 1);
            saveContribAndReduceHp(allianceId, userId, contrib, 2, callback);
        }, callback);
    }

    /** Registruj uspešan udarac u regularnoj borbi (max 10 udarca × 2 HP = max 20 HP). */
    public void registerSuccessfulBossHit(String allianceId, String userId, String username, OnHpReduced callback) {
        loadOrCreateContrib(allianceId, userId, username, contrib -> {
            if (contrib.getSuccessfulHits() >= 10) { callback.onAlreadyMaxed(); return; }
            contrib.setSuccessfulHits(contrib.getSuccessfulHits() + 1);
            saveContribAndReduceHp(allianceId, userId, contrib, 2, callback);
        }, callback);
    }

    /**
     * Registruj rešen zadatak.
     * Lak/normalan/važan (diffXP<=3 ili impXP<=3): lak+normalan=2HP, ostalo=1HP; ukupno max 10 HP.
     * Ostali zadaci: max 6 puta × 4 HP.
     */
    public void registerTaskCompleted(String allianceId, String userId, String username,
                                      int difficultyXP, int importanceXP, OnHpReduced callback) {
        loadOrCreateContrib(allianceId, userId, username, contrib -> {
            boolean isEasy = (difficultyXP <= 3 || importanceXP <= 3);
            int hpDmg;
            if (isEasy) {
                int taskHp = (difficultyXP <= 3 && importanceXP == 1) ? 2 : 1;
                int remaining = 10 - contrib.getEasyTasksHpContrib();
                if (remaining <= 0) { callback.onAlreadyMaxed(); return; }
                hpDmg = Math.min(taskHp, remaining);
                contrib.setEasyTasksHpContrib(contrib.getEasyTasksHpContrib() + hpDmg);
            } else {
                if (contrib.getOtherTasksCount() >= 6) { callback.onAlreadyMaxed(); return; }
                contrib.setOtherTasksCount(contrib.getOtherTasksCount() + 1);
                hpDmg = 4;
            }
            saveContribAndReduceHp(allianceId, userId, contrib, hpDmg, callback);
        }, callback);
    }

    /** Registruj slanje poruke za određeni dan (format "yyyy-MM-dd"). Po danu = 4 HP. */
    public void registerMessageDay(String allianceId, String userId, String username,
                                   String day, OnHpReduced callback) {
        loadOrCreateContrib(allianceId, userId, username, contrib -> {
            if (contrib.getMessageDays() == null) contrib.setMessageDays(new ArrayList<>());
            if (contrib.getMessageDays().contains(day)) { callback.onAlreadyMaxed(); return; }
            contrib.getMessageDays().add(day);
            saveContribAndReduceHp(allianceId, userId, contrib, 4, callback);
        }, callback);
    }

    /** Označi da korisnik ima neurađen zadatak (gubi bonus 10 HP). */
    public void markUndoneTask(String allianceId, String userId, String username) {
        loadOrCreateContrib(allianceId, userId, username, contrib -> {
            if (!contrib.isHasUndoneTask()) {
                contrib.setHasUndoneTask(true);
                db.collection("alliances").document(allianceId)
                        .collection("contributions").document(userId).set(contrib);
            }
        }, null);
    }

    /** Završi misiju: primeni "bez neurađenih" bonus, vrati rezultat. */
    public void finishMission(String allianceId, OnMissionFinished callback) {
        db.collection("alliances").document(allianceId).get()
                .addOnSuccessListener(doc -> {
                    int currentHp = 0;
                    Long hpObj = doc.getLong("missionBossCurrentHp");
                    if (hpObj != null) currentHp = hpObj.intValue();
                    final int finalHp = currentHp;

                    getAllContributions(allianceId, new OnAllContributionsLoaded() {
                        @Override
                        public void onLoaded(List<com.example.myapplication.data.model.MissionContribution> contributions) {
                            // Svaki ko nije imao neurađen dobija 10 HP bonus
                            int[] bonusHp = {0};
                            for (com.example.myapplication.data.model.MissionContribution c : contributions) {
                                if (!c.isHasUndoneTask()) bonusHp[0] += 10;
                            }
                            int finalBossHp = Math.max(0, finalHp - bonusHp[0]);
                            boolean won = finalBossHp <= 0;

                            db.collection("alliances").document(allianceId)
                                    .update("missionActive", false, "missionEndTime", 0L,
                                            "missionBossCurrentHp", finalBossHp);
                            callback.onFinished(won, contributions);
                        }
                        @Override
                        public void onError(String message) { callback.onError(message); }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // --- Privatni helper-i za misiju ---

    private interface OnContribLoaded {
        void onLoaded(com.example.myapplication.data.model.MissionContribution contrib);
    }

    private void loadOrCreateContrib(String allianceId, String userId, String username,
                                     OnContribLoaded onLoaded, OnHpReduced errCallback) {
        db.collection("alliances").document(allianceId)
                .collection("contributions").document(userId).get()
                .addOnSuccessListener(doc -> {
                    com.example.myapplication.data.model.MissionContribution c = doc.exists()
                            ? doc.toObject(com.example.myapplication.data.model.MissionContribution.class)
                            : new com.example.myapplication.data.model.MissionContribution(userId, username, allianceId);
                    if (c == null) c = new com.example.myapplication.data.model.MissionContribution(userId, username, allianceId);
                    onLoaded.onLoaded(c);
                })
                .addOnFailureListener(e -> { if (errCallback != null) errCallback.onError(e.getMessage()); });
    }

    private void saveContribAndReduceHp(String allianceId, String userId,
                                        com.example.myapplication.data.model.MissionContribution contrib,
                                        int hpDmg, OnHpReduced callback) {
        db.collection("alliances").document(allianceId)
                .collection("contributions").document(userId).set(contrib)
                .addOnSuccessListener(v -> {
                    db.collection("alliances").document(allianceId).get()
                            .addOnSuccessListener(doc -> {
                                Long cur = doc.getLong("missionBossCurrentHp");
                                if (cur == null) { callback.onError("Nema aktivne misije"); return; }
                                int newHp = Math.max(0, cur.intValue() - hpDmg);
                                db.collection("alliances").document(allianceId)
                                        .update("missionBossCurrentHp", newHp)
                                        .addOnSuccessListener(a -> callback.onSuccess(hpDmg, newHp))
                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // --- Interfejsi za misiju ---

    public interface OnMissionContributionLoaded {
        void onLoaded(com.example.myapplication.data.model.MissionContribution contribution);
        void onError(String message);
    }

    public interface OnAllContributionsLoaded {
        void onLoaded(List<com.example.myapplication.data.model.MissionContribution> contributions);
        void onError(String message);
    }

    public interface OnHpReduced {
        void onSuccess(int hpDamage, int newBossHp);
        void onAlreadyMaxed();
        void onError(String message);
    }

    public interface OnMissionFinished {
        void onFinished(boolean won, List<com.example.myapplication.data.model.MissionContribution> contributions);
        void onError(String message);
    }
}