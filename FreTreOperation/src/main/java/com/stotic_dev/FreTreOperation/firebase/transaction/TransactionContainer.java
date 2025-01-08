package com.stotic_dev.FreTreOperation.firebase.transaction;

import com.google.cloud.firestore.WriteBatch;

public interface TransactionContainer {
    WriteBatch getBatch();
}
