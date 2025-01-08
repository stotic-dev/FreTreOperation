package com.stotic_dev.FreTreOperation.firebase.query;

import com.google.cloud.firestore.Query;

import java.util.List;

public class EqualInQuery<Value> implements FirestoreQuery {

    private String field;
    private List<Value> valueList;

    public EqualInQuery(String field, List<Value> valueList) {
        this.field = field;
        this.valueList = valueList;
    }

    @Override
    public Query createQuery(Query ref) {
        return ref.whereIn(field, valueList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EqualInQuery<?>) {
            return ((EqualInQuery<?>) obj).field.equals(this.field) && ((EqualInQuery<?>) obj).valueList.equals(this.valueList);
        }
        else {
            return false;
        }
    }
}
