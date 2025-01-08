package com.stotic_dev.FreTreOperation.dto;

import com.stotic_dev.FreTreOperation.BatchApplicationRunner;
import com.stotic_dev.FreTreOperation.constant.JobType;
import com.stotic_dev.FreTreOperation.constant.OperationMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;

public class OperationArgumentDto {

    Logger logger = LogManager.getLogger(BatchApplicationRunner.class);

    public final OperationMode operationMode;
    public final JobType jobType;

    public OperationArgumentDto(ApplicationArguments arguments) {
        logger.info(String.format("Start Parse Arguments: %s",
                String.join(", ", arguments.getSourceArgs())));

        // 運用種別の解析
        if (arguments.getNonOptionArgs().isEmpty()) {
            this.operationMode = null;
            this.jobType = null;
            logger.error("Argument is invalid.");
            return;
        }

        String operationModeStr = arguments.getNonOptionArgs().get(0);
        this.operationMode = OperationMode.getInstance(operationModeStr);

        // 実行するジョブ種別の解析
        if (arguments.getNonOptionArgs().size() <= 1) {
            this.jobType = null;
            logger.error("Argument is invalid.");
            return;
        }

        String jobTypeStr = arguments.getNonOptionArgs().get(1);
        this.jobType = JobType.getInstance(jobTypeStr);

        logger.info("Complete Parse Argument!");
    }
}
