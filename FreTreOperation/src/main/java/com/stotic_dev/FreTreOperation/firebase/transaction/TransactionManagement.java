package com.stotic_dev.FreTreOperation.firebase.transaction;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface TransactionManagement {
    void startTransaction() throws IOException;
    void commitTransaction() throws ExecutionException, InterruptedException;
}
