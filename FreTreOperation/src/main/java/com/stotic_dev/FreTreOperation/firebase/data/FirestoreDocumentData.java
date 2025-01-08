package com.stotic_dev.FreTreOperation.firebase.data;

import java.util.Map;

public class FirestoreDocumentData {
    public final String documentId;
    private final Map<String, Object> data;

    public FirestoreDocumentData(String documentId, Map<String, Object> data) {
        this.documentId = documentId;
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
