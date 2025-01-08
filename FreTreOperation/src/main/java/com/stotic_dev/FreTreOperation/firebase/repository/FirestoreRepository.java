package com.stotic_dev.FreTreOperation.firebase.repository;

import com.stotic_dev.FreTreOperation.firebase.collection.FirestoreCollection;
import com.stotic_dev.FreTreOperation.firebase.data.FirestoreDocumentData;
import com.stotic_dev.FreTreOperation.firebase.query.FirestoreQuery;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface FirestoreRepository {
    List<FirestoreDocumentData> fetchDocuments(FirestoreCollection collection) throws ExecutionException, InterruptedException, TimeoutException;
    List<FirestoreDocumentData> fetchDocuments(FirestoreCollection collection, FirestoreQuery query) throws ExecutionException, InterruptedException, TimeoutException;
    Optional<FirestoreDocumentData> fetchDocument(FirestoreCollection collection, String documentId) throws ExecutionException, InterruptedException;
    void deleteDocuments(FirestoreCollection collection, FirestoreQuery query) throws ExecutionException, InterruptedException, TimeoutException;
    void deleteDocument(FirestoreCollection collection, String documentId) throws ExecutionException, InterruptedException;
    void deleteDocumentsWithTransaction(FirestoreCollection collection, FirestoreQuery query) throws ExecutionException, InterruptedException, TimeoutException;
    void deleteDocumentWithTransaction(FirestoreCollection collection, String documentId);
    String insertDocument(FirestoreCollection collection, Object data) throws ExecutionException, InterruptedException;
    String insertDocument(FirestoreCollection collection, Object data, String documentId) throws ExecutionException, InterruptedException;
}


