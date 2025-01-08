package com.stotic_dev.FreTreOperation.firebase.transaction;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreTransactionClient implements TransactionManagement, TransactionContainer {

    private Optional<WriteBatch> currentBatch = Optional.empty();

    public void startTransaction() throws IOException {
        Firestore db = FirestoreClient.getFirestore();
        currentBatch = Optional.of(db.batch());
    }

    public void commitTransaction() throws ExecutionException, InterruptedException {
        if (currentBatch.isEmpty()) {
            return;
        }

        currentBatch.get().commit().get();
    }

    public WriteBatch getBatch() {
        return currentBatch.orElseThrow();
    }
}
