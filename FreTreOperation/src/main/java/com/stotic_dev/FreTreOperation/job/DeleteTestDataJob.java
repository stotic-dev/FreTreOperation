package com.stotic_dev.FreTreOperation.job;

import com.stotic_dev.FreTreOperation.constant.OperationMode;
import com.stotic_dev.FreTreOperation.firebase.repository.ChatMessageRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.repository.TokenMemberInfoRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.repository.UserInfoRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteTestDataJob implements OperationJob {

    Logger logger = LogManager.getLogger(DeleteTestDataJob.class);
    @Autowired
    UserInfoRepositoryImpl userInfoRepository;
    @Autowired
    TokenMemberInfoRepositoryImpl tokenMemberInfoRepository;
    @Autowired
    ChatMessageRepositoryImpl chatMessageRepository;

    @Override
    public void execute(OperationMode mode) {
        logger.info("[In]");
        if (!mode.isTestMode()) {
            logger.info("Not test mode.");
            return;
        }

        try {
            tokenMemberInfoRepository.deleteAllTokenData();
            userInfoRepository.deleteAllUserInfo();
            chatMessageRepository.deleteAllChatMessage();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("データ削除処理に失敗しました(reason: %s)", e.getMessage()));
        }

        logger.info("[End] Completed delete test data!");
    }
}
