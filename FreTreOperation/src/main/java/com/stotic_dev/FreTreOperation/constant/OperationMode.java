package com.stotic_dev.FreTreOperation.constant;

import org.springframework.lang.NonNull;

public enum OperationMode {

    TEST,
    DEV,
    STG,
    PRD;

    static public @NonNull OperationMode getInstance(String mode) {
        switch (mode) {
            case "test" -> {
                return OperationMode.TEST;
            }
            case "develop" -> {
                return OperationMode.DEV;
            }
            case "staging" -> {
                return OperationMode.STG;
            }
            case "production" -> {
                return OperationMode.PRD;
            }
        }

        return null;
    }

    public boolean isTestMode() {
        return this == OperationMode.TEST || this == OperationMode.DEV;
    }

    public boolean isDevMode() { return this == OperationMode.DEV; }
}
