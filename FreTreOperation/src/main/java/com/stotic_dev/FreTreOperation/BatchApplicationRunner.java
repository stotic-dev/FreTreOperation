package com.stotic_dev.FreTreOperation;

import com.stotic_dev.FreTreOperation.dto.OperationArgumentDto;
import com.stotic_dev.FreTreOperation.firebase.FirebaseConfigRegister;
import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;
import com.stotic_dev.FreTreOperation.job.CleanUnnecessaryFirebaseDataJob;
import com.stotic_dev.FreTreOperation.job.DeleteTestDataJob;
import com.stotic_dev.FreTreOperation.job.InsertTestDataJob;
import com.stotic_dev.FreTreOperation.job.OperationJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BatchApplicationRunner implements ApplicationRunner {

    Logger logger = LogManager.getLogger(BatchApplicationRunner.class);

    @Autowired
    FirebaseConfigRegister firebaseConfigRegister;
    @Autowired
    CleanUnnecessaryFirebaseDataJob cleanUnnecessaryFirebaseDataJob;
    @Autowired
    InsertTestDataJob insertTestDataJob;
    @Autowired
    DeleteTestDataJob deleteTestDataJob;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Start FreTreOperation Batch");

        OperationArgumentDto argumentDto = new OperationArgumentDto(args);
        if (argumentDto.jobType == null) { return; }

        firebaseConfigRegister.build();
        CollectionType.operationMode = argumentDto.operationMode.toString();
        callJob(argumentDto);

        logger.info("Complete FreTreOperation Batch");
    }

    private void callJob(OperationArgumentDto argumentDto) throws Exception {
        OperationJob selectJob = null;
        switch (argumentDto.jobType) {
            case CLEAN_UNNECESSARY_DATA -> selectJob = cleanUnnecessaryFirebaseDataJob;
            case INSERT_TEST_DATA -> selectJob = insertTestDataJob;
            case DELETE_TEST_DATA -> selectJob = deleteTestDataJob;
        }

        if (selectJob == null) {
            throw new RuntimeException(String.format("不正なジョブ種別です(job=%s)", argumentDto.jobType.toString()));
        }

        selectJob.execute(argumentDto.operationMode);
    }
}
