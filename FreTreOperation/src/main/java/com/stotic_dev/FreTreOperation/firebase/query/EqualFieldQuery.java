package com.stotic_dev.FreTreOperation.firebase.query;

import com.google.cloud.firestore.Query;

public class EqualFieldQuery<Value> implements FirestoreQuery {

    private String field;
    private Value value;

    public EqualFieldQuery(String field, Value value) {

        this.field = field;
        this.value = value;
    }

    @Override
    public Query createQuery(Query ref) {
        return ref.whereEqualTo(field, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EqualFieldQuery<?>) {
            return ((EqualFieldQuery<?>) obj).field.equals(this.field) && ((EqualFieldQuery<?>) obj).value.equals(this.value);
        }
        else {
            return false;
        }
    }
}
