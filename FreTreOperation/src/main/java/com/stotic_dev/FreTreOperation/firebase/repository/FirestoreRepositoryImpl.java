package com.stotic_dev.FreTreOperation.firebase.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.stotic_dev.FreTreOperation.firebase.collection.FirestoreCollection;
import com.stotic_dev.FreTreOperation.firebase.data.FirestoreDocumentData;
import com.stotic_dev.FreTreOperation.firebase.query.FirestoreQuery;
import com.stotic_dev.FreTreOperation.firebase.transaction.FirestoreTransactionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.function.ThrowingFunction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Repository
public class FirestoreRepositoryImpl implements FirestoreRepository {
    private final Long timeout = 10L;
    private final int deleteBatchSize = 1000;

    @Autowired
    FirestoreTransactionClient firestoreTransactionClient;

    @Override
    public List<FirestoreDocumentData> fetchDocuments(FirestoreCollection collection) throws ExecutionException, InterruptedException, TimeoutException {
        return getCollectionRef(collection).get()
                .get(timeout, TimeUnit.SECONDS)
                .getDocuments()
                .stream()
                .map(snapshot -> new FirestoreDocumentData(snapshot.getId(), snapshot.getData()))
                .toList();
    }

    public List<FirestoreDocumentData> fetchDocuments(FirestoreCollection collection, FirestoreQuery query) throws ExecutionException, InterruptedException, TimeoutException {
        return query.createQuery(getCollectionRef(collection)).get()
                .get(timeout, TimeUnit.SECONDS)
                .getDocuments()
                .stream()
                .map(snapshot -> new FirestoreDocumentData(snapshot.getId(), snapshot.getData()))
                .toList();
    }

    @Override
    public Optional<FirestoreDocumentData> fetchDocument(FirestoreCollection collection, String documentId) throws ExecutionException, InterruptedException {
        DocumentSnapshot data = getCollectionRef(collection).document(documentId).get().get();

        if (data == null) { return Optional.empty(); }
        return Optional.of(new FirestoreDocumentData(data.getId(), data.getData()));
    }

    @Override
    public void deleteDocuments(FirestoreCollection collection, FirestoreQuery query) throws ExecutionException, InterruptedException, TimeoutException {
        deleteDocuments(collection, query, documentRef -> {
            deleteDocument(documentRef);
            return null;
        });
    }

    @Override
    public void deleteDocument(FirestoreCollection collection, String documentId) throws ExecutionException, InterruptedException {
        deleteDocument(getCollectionRef(collection).document(documentId));
    }

    @Override
    public void deleteDocumentsWithTransaction(FirestoreCollection collection, FirestoreQuery query) throws ExecutionException, InterruptedException, TimeoutException {
        deleteDocuments(collection, query, documentRef -> {
            deleteDocumentWithTransaction(documentRef);
            return null;
        });
    }

    @Override
    public void deleteDocumentWithTransaction(FirestoreCollection collection, String documentId) {
        deleteDocumentWithTransaction(getCollectionRef(collection).document(documentId));
    }

    @Override
    public String insertDocument(FirestoreCollection collection, Object data) throws ExecutionException, InterruptedException {
        collection.printDebugDescription();
        return getCollectionRef(collection).add(data).get().getId();
    }

    @Override
    public String insertDocument(FirestoreCollection collection, Object data, String documentId) throws ExecutionException, InterruptedException {
        collection.printDebugDescription();
        getCollectionRef(collection).document(documentId).create(data).get();
        return documentId;
    }

    private void deleteDocuments(FirestoreCollection collection,
                                 FirestoreQuery query,
                                 ThrowingFunction<DocumentReference, Void> deleteDocument) throws ExecutionException, InterruptedException, TimeoutException {
        // retrieve a small batch of documents to avoid out-of-memory errors
        ApiFuture<QuerySnapshot> future = query.createQuery(getCollectionRef(collection)).limit(deleteBatchSize).get();
        int deleted = 0;
        // future.get() blocks on document retrieval
        List<QueryDocumentSnapshot> documents = future.get(timeout, TimeUnit.SECONDS).getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            deleteDocument.apply(document.getReference());
            ++deleted;
        }
        if (deleted >= deleteBatchSize) {
            // retrieve and delete another batch
            deleteDocuments(collection, query, deleteDocument);
        }
    }

    private void deleteDocument(DocumentReference ref) throws ExecutionException, InterruptedException {
        ref.delete().get();
    }

    private void deleteDocumentWithTransaction(DocumentReference ref) {
        firestoreTransactionClient.getBatch().delete(ref);
    }

    private CollectionReference getCollectionRef(FirestoreCollection collection) {
        if (!collection.hasParent()) {
            // 引数のコレクションがルートコレクションの場合は、小要素がないためそのままReferenceを生成する
            return FirestoreClient.getFirestore().collection(collection.getCollectionId());
        }

        FirestoreCollection targetCollection = collection.getRootCollection();
        CollectionReference ref = FirestoreClient.getFirestore().collection(targetCollection.getCollectionId());

        for (FirestoreCollection currentCollection : targetCollection.getCollectionPath()) {
            if (!currentCollection.hasParent()) { break; }
            ref = ref.document(currentCollection.getDocumentId().get()).collection(currentCollection.getCollectionId());
        }

        return ref.document(collection.getDocumentId().get()).collection(collection.getCollectionId());
    }
}
