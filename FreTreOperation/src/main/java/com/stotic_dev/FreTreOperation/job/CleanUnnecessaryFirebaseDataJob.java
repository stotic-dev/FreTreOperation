package com.stotic_dev.FreTreOperation.job;

import com.stotic_dev.FreTreOperation.constant.OperationMode;
import com.stotic_dev.FreTreOperation.firebase.data.ChatMessageData;
import com.stotic_dev.FreTreOperation.firebase.data.TokenData;
import com.stotic_dev.FreTreOperation.firebase.data.TokenMemberData;
import com.stotic_dev.FreTreOperation.firebase.data.UserInfoData;
import com.stotic_dev.FreTreOperation.firebase.repository.ChatMessageRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.repository.TokenMemberInfoRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.repository.UserInfoRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.transaction.FirestoreTransactionClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class CleanUnnecessaryFirebaseDataJob implements OperationJob {

    // Private property
    private final Logger logger = LogManager.getLogger(CleanUnnecessaryFirebaseDataJob.class);
    private final long dataRetentionPeriod = 31536000;

    // Dependency
    @Autowired
    FirestoreTransactionClient firestoreTransactionClient;
    @Autowired
    UserInfoRepositoryImpl userInfoRepository;
    @Autowired
    TokenMemberInfoRepositoryImpl tokenMemberInfoRepository;
    @Autowired
    ChatMessageRepositoryImpl chatMessageRepository;

    @Override
    public void execute(OperationMode mode) {
        logger.info(String.format("[In] mode=%s", mode.toString()));

        try {
            firestoreTransactionClient.startTransaction();
            doCleaningFirestoreData();
            firestoreTransactionClient.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("クリーニングジョブが失敗しました。処理途中の処理をロールバックします。(reason: %s)", e.getMessage()));
        }

        logger.info("[End]");
    }

    private void doCleaningFirestoreData() throws Exception {
        List<UserInfoData> allUsers = userInfoRepository.fetchAll();
        logger.info(String.format("Fetched user data: %s", allUsers.stream().map(UserInfoData::getMemberId).collect(Collectors.joining(", "))));

        // ターゲットのユーザー情報を削除
        List<UserInfoData> deleteTargetUser = getDeleteTargetUserInfoList(allUsers);
        if (deleteTargetUser.isEmpty()) {
            logger.info("Skip cleaning because no delete target user.");
            return;
        }
        deleteUserInfoData(deleteTargetUser);

        // 全てのチーム情報の取得
        List<TokenData> allTeam = tokenMemberInfoRepository.fetchALLTokenMembersPerTeam();
        logger.info(String.format("Fetched team data: %s", allTeam.stream().map(team -> team.teamId).collect(Collectors.joining(", "))));

        // ターゲットのチーム情報を削除
        List<TokenData> deleteTargetTokenMemberData = getDeleteTargetTokenTeamMemberList(allTeam, deleteTargetUser);
        List<String> deleteTargetTokenTeamUUIDList = getDeleteTargetTokenTeamUUIDList(deleteTargetTokenMemberData, allTeam);
        logger.info(String.format("Delete target team: %s", deleteTargetTokenTeamUUIDList.stream().collect(Collectors.joining(", "))));
        deleteTokenTeam(deleteTargetTokenMemberData, deleteTargetTokenTeamUUIDList);

        // チャットメッセージの情報を削除
        List<ChatMessageData> deleteTargetChatMessageList = getDeleteTargetChatMessageList(chatMessageRepository.fetchAllChatMessage(), deleteTargetTokenTeamUUIDList);
        logger.info(String.format("Delete target chat message count: %s", deleteTargetChatMessageList.size()));
        deleteChatMessage(getDeleteTargetChatMessageList(deleteTargetChatMessageList, deleteTargetTokenTeamUUIDList));
    }

    private List<UserInfoData> getDeleteTargetUserInfoList(List<UserInfoData> allUsers) {
        Instant currentDateTime = Instant.now();
        List<UserInfoData> deleteTargetUser = allUsers.stream()
                .filter(user -> dataRetentionPeriod < currentDateTime.getEpochSecond() - user.getUpdateDateTime().getEpochSecond())
                .toList();
        logger.info(String.format("Get delete target users: %s, count: %s",
                deleteTargetUser.stream().map(UserInfoData::getMemberId).collect(Collectors.joining(", ")),
                deleteTargetUser.size()));
        return deleteTargetUser;
    }

    private List<TokenData> getDeleteTargetTokenTeamMemberList(List<TokenData> allTokenTeam, List<UserInfoData> deleteTargetUserList) {
        List<TokenData> deleteTargetTokenTeamList = allTokenTeam.stream()
                .map(team -> {
                    List<TokenMemberData> deleteTargetMember = team.getMembers()
                            .stream()
                            .filter(member -> deleteTargetUserList.stream().map(UserInfoData::getMemberId).toList().contains(member.memberId))
                            .toList();
                    return new TokenData(team.teamId, team.teamUUID, deleteTargetMember);
                })
                .toList();
        logger.info(String.format("Get delete target(%s), count: %s",
                deleteTargetTokenTeamList.stream().map(team -> String.format("(team=%s, memberIds=%s)", team.teamUUID, team.getMembers().stream().map(member -> member.memberId).collect(Collectors.joining(", ")))).collect(Collectors.joining(", ")),
                deleteTargetTokenTeamList.size()));
        return deleteTargetTokenTeamList;
    }

    private List<String> getDeleteTargetTokenTeamUUIDList(List<TokenData> deleteTargetTokenTeamMemberList, List<TokenData> allTokenTeam) {
        return deleteTargetTokenTeamMemberList
                .stream()
                .filter(deleteTarget -> {
                    int targetIndex = deleteTargetTokenTeamMemberList.indexOf(deleteTarget);
                    return deleteTarget.getMembers().size() == allTokenTeam.get(targetIndex).getMembers().size();
                })
                .map(tokenData -> tokenData.teamUUID)
                .toList();
    }

    private List<ChatMessageData> getDeleteTargetChatMessageList(List<ChatMessageData> allChatMessageList, List<String> deleteTargetTeamUUIDList) {
        return allChatMessageList
                .stream()
                .filter(chat -> deleteTargetTeamUUIDList.contains(chat.teamUUID))
                .toList();
    }

    private void deleteUserInfoData(List<UserInfoData> deleteTargetUser) throws ExecutionException, InterruptedException, TimeoutException {
        userInfoRepository.deleteUserInfoListByMemberId(deleteTargetUser.stream().map(UserInfoData::getMemberId).toList());
        logger.info("Deleted target user info data.");
    }

    private void deleteTokenTeam(List<TokenData> deleteTargetTokenMemberData, List<String> deleteTargetTeamUUID) throws ExecutionException, InterruptedException, TimeoutException {
        for (TokenData team : deleteTargetTokenMemberData) {
            // チームメンバー情報を削除
            for (TokenMemberData member : team.getMembers()) {
                tokenMemberInfoRepository.deleteTokenTeamMember(team.teamUUID, member.memberId);
            }

            // チーム情報を削除
            if (deleteTargetTeamUUID.contains(team.teamUUID)) {
                tokenMemberInfoRepository.deleteTokenTeam(team.teamUUID);
            }
        }
        logger.info("Deleted target token team data.");
    }

    private void deleteChatMessage(List<ChatMessageData> deleteTargetChatMessageList) throws Exception {
        for (String teamUUID : deleteTargetChatMessageList.stream().map(chat -> chat.teamUUID).toList()) {
            chatMessageRepository.deleteChatMessageByTeamUUID(teamUUID);
        }
        logger.info("Deleted chat message data.");
    }
}
