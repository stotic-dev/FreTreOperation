package com.stotic_dev.FreTreOperation.firebase.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenData {
    public final String teamId;
    public final String teamUUID;
    private final List<TokenMemberData> members;

    public TokenData(String teamId, String teamUUID, List<TokenMemberData> members) {
        this.teamId = teamId;
        this.teamUUID = teamUUID;
        this.members = members;
    }

    public TokenData(Map<String, Object> tokenData, String teamUUID, List<TokenMemberData> members) {
        this.teamId = (String) tokenData.get("teamId");
        this.teamUUID = teamUUID;
        this.members = members;
    }

    public Map<String, Object> getFirestoreData() {
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("teamId", teamId);
        return firestoreData;
    }

    public List<TokenMemberData> getMembers() {
        return members;
    }
}
