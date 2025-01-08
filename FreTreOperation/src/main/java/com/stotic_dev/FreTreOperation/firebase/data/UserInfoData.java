package com.stotic_dev.FreTreOperation.firebase.data;

import com.google.cloud.Timestamp;
import com.stotic_dev.FreTreOperation.firebase.constant.FirebaseSystemError;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserInfoData {
    private final String memberId;
    private final String userName;
    private final String birthDay;
    private final double weight;
    private final double height;
    private final boolean sex;
    private final String iconImageUrl;
    private final double goalWeight;
    private final double startWeight;
    private final int activeLevel;
    private final boolean isPrivate;
    private final Timestamp updateDateTime;

    public UserInfoData(String memberId,
                        String userName,
                        String birthDay,
                        double weight,
                        double height,
                        boolean sex,
                        String iconImageUrl,
                        double goalWeight,
                        double startWeight,
                        int activeLevel,
                        boolean isPrivate,
                        Instant updateDateTime) {
        this.memberId = memberId;
        this.userName = userName;
        this.birthDay = birthDay;
        this.weight = weight;
        this.height = height;
        this.sex = sex;
        this.iconImageUrl = iconImageUrl;
        this.goalWeight = goalWeight;
        this.startWeight = startWeight;
        this.activeLevel = activeLevel;
        this.isPrivate = isPrivate;
        this.updateDateTime = Timestamp.of(Date.from(updateDateTime));
    }

    private UserInfoData(Map<String, Object> data) throws FirebaseSystemError {
        memberId = (String) data.get("memberId");
        userName = (String) data.get("userName");
        birthDay = (String) data.get("birthDay");
        weight = ((Number) data.get("weight")).doubleValue();
        height = ((Number) data.get("height")).doubleValue();
        sex = (boolean) data.get("sex");
        iconImageUrl = (String) data.get("iconImageUrl");
        goalWeight = ((Number) data.get("goalWeight")).doubleValue();
        startWeight = ((Number) data.get("startWeight")).doubleValue();
        activeLevel = ((Number) data.get("activeLevel")).intValue();
        isPrivate = (boolean) data.get("isPrivate");
        updateDateTime = (Timestamp) data.get("updateDateTime");

        if(memberId == null || userName == null || birthDay == null || iconImageUrl == null) {
            throw new FirebaseSystemError("Invalid userInfo data.");
        }
    }

    public static Optional<UserInfoData> buildFromFirestoreData(Map<String, Object> data) {
        try {
            return Optional.of(new UserInfoData(data));
        } catch (FirebaseSystemError e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }

    public Map<String, Object> toFirestoreData() {
        Map<String, Object> firestoreData = new HashMap<>();
        firestoreData.put("memberId", memberId);
        firestoreData.put("userName", userName);
        firestoreData.put("birthDay", birthDay);
        firestoreData.put("weight", weight);
        firestoreData.put("height", height);
        firestoreData.put("sex", sex);
        firestoreData.put("iconImageUrl", iconImageUrl);
        firestoreData.put("goalWeight", goalWeight);
        firestoreData.put("startWeight", startWeight);
        firestoreData.put("activeLevel", activeLevel);
        firestoreData.put("isPrivate", isPrivate);
        firestoreData.put("updateDateTime", updateDateTime);
        return firestoreData;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getUserName() {
        return userName;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public double getWeight() {
        return weight;
    }

    public double getHeight() {
        return height;
    }

    public boolean isSex() {
        return sex;
    }

    public String getIconImageUrl() {
        return iconImageUrl;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public double getStartWeight() {
        return startWeight;
    }

    public int getActiveLevel() {
        return activeLevel;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public Instant getUpdateDateTime() {
        return updateDateTime.toDate().toInstant();
    }
}
