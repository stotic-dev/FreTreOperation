package com.stotic_dev.FreTreOperation.firebase.query;

import com.google.cloud.firestore.Query;

public class AnyQuery implements FirestoreQuery {

    public AnyQuery() {}

    @Override
    public Query createQuery(Query ref) {
        return ref;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyQuery;
    }
}
