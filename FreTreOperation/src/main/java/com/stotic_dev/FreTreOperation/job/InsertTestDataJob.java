package com.stotic_dev.FreTreOperation.job;

import com.stotic_dev.FreTreOperation.constant.OperationMode;
import com.stotic_dev.FreTreOperation.firebase.data.ChatMessageData;
import com.stotic_dev.FreTreOperation.firebase.data.TokenData;
import com.stotic_dev.FreTreOperation.firebase.data.TokenMemberData;
import com.stotic_dev.FreTreOperation.firebase.data.UserInfoData;
import com.stotic_dev.FreTreOperation.firebase.repository.ChatMessageRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.repository.TokenMemberInfoRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.repository.UserInfoRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public class InsertTestDataJob implements OperationJob {

    Logger logger = LogManager.getLogger(InsertTestDataJob.class);
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

        String memberId = UUID.randomUUID().toString();
        String deleteMemberId = UUID.randomUUID().toString();

        try {
            String teamUUID = insertTokenData("teamName1", memberId);
            String deleteTeamUUID = insertTokenData("teamName2", deleteMemberId);
            insertUserData(false, memberId);
            insertUserData(true, deleteMemberId);
            insertChatData(teamUUID, deleteTeamUUID);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("データ挿入処理に失敗しました(reason: %s)", e.getMessage()));
        }

        logger.info("[End]");
    }

    private String insertTokenData(String teamName, String... memberIds) throws ExecutionException, InterruptedException, TimeoutException {
        TokenData tokenData1 = new TokenData(
                teamName,
                UUID.randomUUID().toString(),
                Arrays.stream(memberIds).toList().stream()
                        .map(memberId -> new TokenMemberData(memberId, UUID.randomUUID().toString()))
                        .toList()
        );
        return tokenMemberInfoRepository.insertTokenData(tokenData1);
    }

    private void insertUserData(boolean isDeleteTarget, String... memberIds) {
        List<UserInfoData> insertDataList = Arrays.stream(memberIds).toList().stream()
                        .map(memberId -> {
                            return new UserInfoData(memberId,
                                    "",
                                    "",
                                    64.2,
                                    173,
                                    false,
                                    "",
                                    56.1,
                                    65,
                                    1,
                                    false,
                                    isDeleteTarget ? Instant.ofEpochSecond(10000) : Instant.now());
                        })
                                .toList();

        userInfoRepository.insertUserInfo(insertDataList);
    }

    private void insertChatData(String... teamUUIDs) throws ExecutionException, InterruptedException {
        List<ChatMessageData> insertChatTextMessageDataList = Arrays.stream(teamUUIDs)
                .toList()
                .stream()
                .map(teamUUID -> new ChatMessageData("userName",
                        "message",
                        "",
                        teamUUID,
                        Instant.now(),
                        "memberId"))
                .toList();
        chatMessageRepository.insertChatMessageData(insertChatTextMessageDataList);

        List<ChatMessageData> insertDataList = Arrays.stream(teamUUIDs)
                .toList()
                .stream()
                .map(teamUUID -> new ChatMessageData("userName",
                        "",
                        "https://example.com/imageUrl.png",
                        teamUUID,
                        Instant.now(),
                        "memberId"))
                .toList();
        chatMessageRepository.insertChatMessageData(insertDataList);
    }
}
