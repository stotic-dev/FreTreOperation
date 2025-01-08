package com.stotic_dev.FreTreOperation.firebase.data;

import com.google.cloud.Timestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ChatMessageData {

    public final String userName;
    public final String message;
    public final String imageUrl;
    public final String teamUUID;
    private final Timestamp createdAt;
    public final String memberId;

    public ChatMessageData(String userName, String message, String imageUrl, String teamUUID, Instant createdAt, String memberId) {
        this.userName = userName;
        this.message = message;
        this.imageUrl = imageUrl;
        this.teamUUID = teamUUID;
        this.createdAt = Timestamp.ofTimeMicroseconds(createdAt.toEpochMilli());
        this.memberId = memberId;
    }

    public ChatMessageData(Map<String, Object> firestoreData) {
        userName = (String) firestoreData.get("userName");
        message = (String) firestoreData.get("message");
        imageUrl = (String) firestoreData.get("imageUrl");
        teamUUID = (String) firestoreData.get("teamUUID");
        createdAt = (Timestamp) firestoreData.get("createdAt");
        memberId = (String) firestoreData.get("memberId");
    }

    public Map<String, Object> getFirestoreData() {
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("userName", userName);
        firestoreData.put("message", message);
        firestoreData.put("imageUrl", imageUrl);
        firestoreData.put("teamUUID", teamUUID);
        firestoreData.put("createdAt", createdAt);
        firestoreData.put("memberId", memberId);
        return firestoreData;
    }

    public Instant getCreatedAt() {
        return createdAt.toDate().toInstant();
    }
}
