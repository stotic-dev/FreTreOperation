package com.stotic_dev.FreTreOperation.constant;

import org.springframework.lang.NonNull;

public enum JobType {

    CLEAN_UNNECESSARY_DATA("clean"),
    INSERT_TEST_DATA("testInsert"),
    DELETE_TEST_DATA("testDelete");

    JobType(String jobParamName) {
        this.jobParamName = jobParamName;
    }

    public static @NonNull JobType getInstance(String jobParamName) {
        switch (jobParamName) {
            case "clean": {
                return JobType.CLEAN_UNNECESSARY_DATA;
            }
            case "testInsert": {
                return JobType.INSERT_TEST_DATA;
            }
            case "testDelete": {
                return JobType.DELETE_TEST_DATA;
            }
        }
        return null;
    }

    private String jobParamName;
}
