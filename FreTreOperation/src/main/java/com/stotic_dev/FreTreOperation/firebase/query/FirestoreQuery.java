package com.stotic_dev.FreTreOperation.firebase.query;

import com.google.cloud.firestore.Query;

public interface FirestoreQuery {
    Query createQuery(Query ref);
}
