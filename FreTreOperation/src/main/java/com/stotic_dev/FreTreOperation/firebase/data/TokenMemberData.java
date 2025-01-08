package com.stotic_dev.FreTreOperation.firebase.data;

import java.util.HashMap;
import java.util.Map;

public class TokenMemberData {
    public final String memberId;
    public final String token;

    public TokenMemberData(String memberId, String token) {
        this.memberId = memberId;
        this.token = token;
    }

    public TokenMemberData(Map<String, Object> firestoreData) {
        this.memberId = (String) firestoreData.get("memberId");
        this.token = (String) firestoreData.get("token");
    }

    public Map<String, Object> getFirestoreData() {
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("memberId", memberId);
        firestoreData.put("token", token);
        return firestoreData;
    }
}
