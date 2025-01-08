package com.stotic_dev.FreTreOperation.firebase.constant;

import com.stotic_dev.FreTreOperation.constant.OperationMode;

import java.util.Optional;

public enum CollectionType {

    TOKEN_TEAM("%s_tokens"),
    TOKEN_MEMBER("%s_members"),
    CHAT("%s_chat"),
    USERINFO("%s_userInfo");

    CollectionType(String collectionId) {

        this.collectionId = collectionId;
    }

    private final String collectionId;

    public static String operationMode = OperationMode.TEST.toString();

    public Optional<CollectionType> getChild() {
        return switch (this) {
            case TOKEN_TEAM -> Optional.of(CollectionType.TOKEN_MEMBER);
            default -> Optional.empty();
        };
    }

    public String getCollectionId() {
        return setPrefix(collectionId);
    }

    private String setPrefix(String collectionId) {
        return String.format(collectionId, operationMode);
    }
}
