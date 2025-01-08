package com.stotic_dev.FreTreOperation.firebase.query;

import com.google.cloud.firestore.Query;

import java.util.List;

public class MultiQuery implements FirestoreQuery {
    private List<FirestoreQuery> queryList;

    public MultiQuery(List<FirestoreQuery> queryList) {
        this.queryList = queryList;
    }

    @Override
    public Query createQuery(Query ref) {
        Query resultQuery = ref;
        for(FirestoreQuery query : queryList) {
            resultQuery = query.createQuery(resultQuery);
        }
        return resultQuery;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MultiQuery) {
            return ((MultiQuery) obj).queryList.equals(this.queryList);
        }
        else {
            return false;
        }
    }
}
